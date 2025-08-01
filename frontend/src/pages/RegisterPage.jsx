import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { saveTokens } from '../utils/auth';
import { apiFetch } from '../utils/auth';

function RegisterPage() {
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [weight, setWeight] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleRegister = async (e) => {
        e.preventDefault();
        setError('');

        try {
            const res = await apiFetch('http://localhost:8080/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, email, password, weight }),
            });

            if (!res.ok) {
                let errorMessage = 'Registration failed';
                try {
                    const data = await res.clone().json();
                    errorMessage = data.message || errorMessage;
                } catch (e) {
                    const text = await res.text();
                    if (text) errorMessage = text;
                }
                throw new Error(errorMessage);
            }

            const { accessToken, refreshToken } = await res.json();
            saveTokens({ accessToken, refreshToken });
            navigate('/login');

        } catch (err) {
            console.error('❌ Register error:', err.message);
            setError(err.message);
        }
    };


    return (
        <div className="min-h-screen flex items-center justify-center bg-peach dark:bg-gray-900 text-gray-800 dark:text-white">
            <div className="relative bg-white dark:bg-gray-800 p-8 rounded-lg shadow-md w-full max-w-md">
                <button
                    type="button"
                    onClick={() => navigate('/')}
                    className="absolute top-2 left-3 text-blue-600 dark:text-blue-400 text-sm hover:underline"
                >
                    ← Back to Homepage
                </button>

                <form onSubmit={handleRegister}>
                    <h2 className="text-2xl font-semibold mb-6 text-center">Register</h2>

                    <div className="mb-4">
                        <label className="block mb-1 font-medium text-left" htmlFor="name">Name</label>
                        <input
                            id="name"
                            type="text"
                            className="w-full border px-4 py-2 rounded bg-white dark:bg-gray-700 text-gray-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-peach"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            required
                        />
                    </div>

                    <div className="mb-4">
                        <label className="block mb-1 font-medium text-left" htmlFor="email">Email</label>
                        <input
                            id="email"
                            type="email"
                            className="w-full border px-4 py-2 rounded bg-white dark:bg-gray-700 text-gray-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-peach"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>

                    <div className="mb-6">
                        <label className="block mb-1 font-medium text-left" htmlFor="password">Password</label>
                        <input
                            id="password"
                            type="password"
                            className="w-full border px-4 py-2 rounded bg-white dark:bg-gray-700 text-gray-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-peach"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>

                    <div className="mb-6">
                        <label className="block mb-1 font-medium text-left" htmlFor="weight">Weight (kg)</label>
                        <input
                            id="weight"
                            type="number"
                            className="w-full border px-4 py-2 rounded bg-white dark:bg-gray-700 text-gray-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-peach"
                            value={weight}
                            onChange={(e) => setWeight(e.target.value)}
                            required
                        />
                    </div>

                    {error && (
                        <div className="text-sm text-red-600 dark:text-red-400 mb-4 text-center">{error}</div>
                    )}

                    <button
                        type="submit"
                        className="w-full bg-green-700 text-white py-2 rounded hover:bg-blue-700 transition"
                    >
                        Register
                    </button>
                </form>

                <p className="text-sm text-center mt-4">
                    Already have an account?{' '}
                    <a href="/login" className="text-blue-600 dark:text-blue-400 hover:underline">
                        Login
                    </a>
                </p>
            </div>
        </div>
    );

}

export default RegisterPage;
