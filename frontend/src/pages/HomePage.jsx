import { useNavigate } from 'react-router-dom';

function HomePage() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen flex flex-col items-center justify-center text-gray-800 bg-peach">
      <h1 className="text-white text-4xl font-bold mb-4 text-center">Meal Planner AI 🍽️</h1>
      <p className="text-white text-lg text-center max-w-xl mb-8">
        Your personal AI-powered meal planner. Generate weekly plans, track nutrition, and auto-create shopping lists — all tailored to your goals.
      </p>
      <div className="flex gap-4">
        <button
          onClick={() => navigate('/login')}
          className="bg-paleblue px-6 py-3 rounded hover:bg-blue-700 transition"
        >
          Login
        </button>
        <button
          onClick={() => navigate('/register')}
          className="bg-paleblue px-6 py-3 rounded hover:bg-blue-700 transition"
        >
          Register
        </button>
        <button
          onClick={() => navigate('/dashboard')}
          className="bg-paleblue px-6 py-3 rounded hover:bg-blue-700 transition"
        >
          Go To Dashboard
        </button>
      </div>
    </div>
  );
}

export default HomePage;
