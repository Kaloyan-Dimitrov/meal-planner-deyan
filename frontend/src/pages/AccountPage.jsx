import { useEffect, useState } from 'react';
import { jwtDecode } from 'jwt-decode';
import { useNavigate } from 'react-router-dom';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import { apiFetch } from '../utils/auth';

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
        apiFetch(`/api/users/${userId}`)
            .then(res => res.json())
            .then(setUser);

        apiFetch(`/api/users/${userId}/weight`)
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
        <div className="min-h-screen bg-peach dark:bg-gray-900 text-gray-800 dark:text-white p-6">
            <header className="flex items-start justify-between mb-6">
                <div className="bg-white dark:bg-gray-800 rounded shadow px-3 pt-2 pb-2 text-center">
                    <h1 className="text-2xl font-bold">My account</h1>
                </div>
                <button
                    onClick={() => navigate("/dashboard")}
                    className="text-blue-600 dark:text-blue-400 hover:underline"
                >
                    ← Back to Dashboard
                </button>
            </header>

            <div className="flex flex-col md:flex-row gap-6">
                {user && (
                    <div className="bg-white dark:bg-gray-800 rounded shadow px-6 pt-6 pb-2 w-full md:w-1/3 text-left h-[170px]">
                        <p className="mb-2"><span className="font-semibold">Name:</span> {user.name}</p>
                        <p className="mb-2"><span className="font-semibold">Email:</span> {user.email}</p>
                        <p className="mb-2">
                            <span className="font-semibold">Day Streak:</span>{' '}
                            <span className="text-green-700 dark:text-green-400 font-medium">🔥 {user.dayStreak}</span>
                        </p>
                        <p>
                            <span className="font-semibold">Current Weight:</span>{' '}
                            <span className="text-gray-800 dark:text-white font-medium">
                                {currentWeight ? `${currentWeight} kg` : '--'}
                            </span>
                        </p>
                    </div>
                )}

                <div className="bg-white dark:bg-gray-800 rounded shadow p-6 w-full flex-1">
                    <h2 className="text-lg font-semibold mb-4">Weight Progress (Last 30 Days)</h2>
                    {weights.length === 0 ? (
                        <p className="text-gray-500 dark:text-gray-400">No weight data yet</p>
                    ) : (
                        <ResponsiveContainer width="100%" height={300}>
                            <LineChart data={weights}>
                                <CartesianGrid strokeDasharray="3 3" strokeOpacity={0.2} />
                                <XAxis dataKey="date" stroke="currentColor" />
                                <YAxis domain={['auto', 'auto']} stroke="currentColor" />
                                <Tooltip contentStyle={{ backgroundColor: '#1f2937', border: 'none', color: '#fff' }} />
                                <Line
                                    type="monotone"
                                    dataKey="weight"
                                    stroke="#38bdf8"
                                    strokeWidth={2}
                                    dot={{ r: 3 }}
                                />
                            </LineChart>
                        </ResponsiveContainer>
                    )}
                </div>
            </div>
        </div>
    );
}
export default AccountPage; 