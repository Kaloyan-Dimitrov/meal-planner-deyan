import { useEffect, useState } from 'react';
import { jwtDecode } from 'jwt-decode';
import { useNavigate } from 'react-router-dom';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';

function AccountPage() {
    const navigate = useNavigate();
    const [user, setUser] = useState("user");
    const [weights, setWeights] = useState([]);
    const [currentWeight, setCurrentWeight] = useState(null);
    const token = localStorage.getItem('jwt');
    const authHeader = token ? { Authorization: `Bearer ${token}` } : {};
    let userId = null;

    if (token) {
        try {
            const claims = jwtDecode(token);
            userId = claims.userId || null;
        } catch (e) {
            console.warn('JWT decode failed', e);
        }
    }
    if (!userId) return <p>Not logged in</p>;

    useEffect(() => {
        fetch(`/api/users/${userId}`, { headers: authHeader })
            .then(res => res.json())
            .then(setUser);

        fetch(`/api/users/${userId}/weight`, { headers: authHeader })
            .then(res => res.json())
            .then(data => {
                // Format dates for chart
                const formatted = data.map(d => ({
                    ...d,
                    date: formatToDayMonth(d.date), // '07-26' instead of '2025-07-26'
                }));
                setWeights(formatted);
                if (formatted.length > 0) {
                    const last = formatted[formatted.length - 1];
                    setCurrentWeight(last.weight);
                }
            });
    }, []);
    function formatToDayMonth(isoDate) {
        const [year, month, day] = isoDate.split('T')[0].split('-'); // '2025-07-27T12:00...' → ['2025','07','27']
        return `${day}/${month}`; // → 27/07
    }

    return (
        <div className="min-h-screen bg-peach text-gray-800 p-6">
            <header className="flex items-start justify-between mb-6">
                <div className="bg-white rounded shadow px-3 pt-2 pb-2 text-center">
                    <h1 className="text-2xl font-bold">My account</h1>
                </div>
                <button
                    onClick={() => navigate("/dashboard")}
                    className="text-blue-600 hover:underline">
                    ← Back to Dashboard
                </button>
            </header>
            <div className="flex flex-col md:flex-row gap-6">
                {/* Profile info - fixed width on desktop */}
                {user && (
                    <div className="bg-white rounded shadow px-6 pt-6 pb-2 w-full md:w-1/3 text-left h-[170px]">
                        <p className="mb-2"><span className="font-semibold">Name:</span> {user.name}</p>
                        <p className="mb-2"><span className="font-semibold">Email:</span> {user.email}</p>
                        <p className="mb-2">
                            <span className="font-semibold">Day Streak:</span>{' '}
                            <span className="text-green-700 font-medium">🔥 {user.dayStreak}</span>
                        </p>
                        <p>
                            <span className="font-semibold">Current Weight:</span>{' '}
                            <span className="text-gray-800 font-medium">
                                {currentWeight ? `${currentWeight} kg` : '--'}
                            </span>
                        </p>
                    </div>
                )}

                {/* Chart */}
                <div className="bg-white rounded shadow p-6 w-full flex-1">
                    <h2 className="text-lg font-semibold mb-4">Weight Progress (Last 30 Days)</h2>
                    {weights.length === 0 ? (
                        <p className="text-gray-500">No weight data yet</p>
                    ) : (
                        <ResponsiveContainer width="100%" height={300}>
                            <LineChart data={weights}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="date" />
                                <YAxis domain={['auto', 'auto']} />
                                <Tooltip />
                                <Line type="monotone" dataKey="weight" stroke="#8884d8" strokeWidth={2} dot={{ r: 3 }} />
                            </LineChart>
                        </ResponsiveContainer>
                    )}
                </div>
            </div>
        </div>
    );
}
export default AccountPage; 