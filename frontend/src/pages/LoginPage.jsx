import { useState } from "react";
import { FaEye, FaEyeSlash } from 'react-icons/fa';
function LoginPage() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false)

    const handleLogin = (e) => {
        e.preventDefault();
        // TODO: connect to backend
        console.log('Login clicked:', { email, password });
    };
    return (
        <div className="min-h-screen flex items-center justify-center bg-peach text-gray-800">
            <form
                onSubmit={handleLogin}
                className="bg-white p-8 rounded-lg shadow-md w-full max-w-md"
            >
                <h2 className="text-2xl font-semibold mb-6 text-center">Login</h2>

                <div className="mb-4">
                    <label className="block mb-1 font-medium text-left" htmlFor="email">
                        Email
                    </label>
                    <input
                        id="email"
                        type="email"
                        className="w-full border px-4 py-2 rounded focus:outline-none focus:ring-2 focus:ring-peach"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                </div>

                <div className="mb-6">
                    <label className="block mb-1 font-medium text-left" htmlFor="password">
                        Password
                    </label>
                    <div className="relative">
                        <input
                            id="password"
                            type={showPassword ? 'text' : 'password'}
                            className="w-full border px-4 py-2 rounded focus:outline-none focus:ring-2 focus:ring-peach"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                        <button
                            type="button"
                            onClick={() => setShowPassword(!showPassword)}
                            className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-600 hover:text-gray-800"
                        >
                            {showPassword ? <FaEyeSlash /> : <FaEye />}
                        </button>
                    </div>
                </div>

                <button
                    type="submit"
                    className="w-full bg-paleblue text-white py-2 rounded hover:bg-blue-700 transition"
                >
                    Login
                </button>
            </form>
        </div>
    );
}
export default LoginPage