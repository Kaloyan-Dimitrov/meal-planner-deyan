import { useState } from "react";
import { FaEye, FaEyeSlash } from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';
function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('');
  const navigate = useNavigate();

const handleLogin = async (e) => {
  e.preventDefault();
  setError('');

  try {
    const res = await fetch('http://localhost:8080/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    });

    const text = await res.text();

    if (!res.ok) {
      try {
        const data = JSON.parse(text);
        throw new Error(data.message || 'Login failed');
      } catch {
        throw new Error(text || 'Login failed');
      }
    }

    const { token } = JSON.parse(text);
    localStorage.setItem('jwt', token);

    console.log('✅ Login success, navigating to /dashboard');
    navigate('/dashboard');
  } catch (err) {
    console.error('❌ Login error:', err.message);
    setError(err.message);
  }
};

  return (
    <div className="min-h-screen flex items-center justify-center bg-peach text-gray-800">
      <div className="relative bg-white p-8 rounded-lg shadow-md w-full max-w-md">

        {/* Top-left link */}
        <button
          type="button"
          onClick={() => navigate('/')}
          className="absolute top-2 left-3 text-blue-600 text-sm hover:underline"
        >
          ← Back to Homepage
        </button>

        <h2 className="text-2xl font-semibold mb-6 text-center">Login</h2>

        {error && (
          <div className="text-sm text-red-600 mb-4 text-center">{error}</div>
        )}

        <form onSubmit={handleLogin}>
          <div className="mb-4">
            <label className="block mb-1 font-medium text-left" htmlFor="email">Email</label>
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
            <label className="block mb-1 font-medium text-left" htmlFor="password">Password</label>
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
                {showPassword ? (
                  <i className="fa-solid fa-eye-slash"></i>
                ) : (
                  <i className="fa-solid fa-eye"></i>
                )}
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

        <p className="text-sm text-center mt-4">
          Don’t have an account?{' '}
          <a href="/register" className="text-blue-600 hover:underline">
            Create One
          </a>
        </p>
      </div>
    </div>
  );
}
export default LoginPage