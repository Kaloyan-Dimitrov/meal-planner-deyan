import { useNavigate } from 'react-router-dom';
import { FaAppleAlt, FaChartLine, FaShoppingCart, FaSun, FaMoon } from 'react-icons/fa';
import { FaBowlFood } from "react-icons/fa6";
import { toggleTheme } from '../utils/theme';

function HomePage() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-peach dark:bg-gray-900 text-gray-800 dark:text-white px-6 py-8 flex flex-col">
      <header className="flex flex-col items-center text-center">
        <button
          onClick={toggleTheme}
          className="absolute top-4 right-4 bg-gray-300 dark:bg-gray-700 text-black dark:text-white p-2 rounded-full z-50"
        >
          <FaSun className="hidden dark:inline" />
          <FaMoon className="dark:hidden" />
        </button>

        <FaBowlFood className="text-5xl text-green-700 dark:text-green-400" />
        <h1 className="text-white dark:text-white text-4xl md:text-5xl font-bold mb-4">
          Meal Planner 🍽️
        </h1>
        <p className="text-white dark:text-gray-300 text-lg md:text-xl max-w-xl">
          Generate weekly meal plans, track macros, and get shopping lists – all powered by AI.
        </p>
      </header>

      <div className="w-full max-w-4xl mx-auto">
        <div className="grid grid-cols-2 gap-8 mb-10 py-8">
          <div className="flex justify-center">
            <button
              onClick={() => navigate('/login')}
              className="bg-green-700 text-white px-6 py-3 rounded hover:bg-green-800 dark:hover:bg-green-600 transition"
            >
              Get Started
            </button>
          </div>
          <div className="flex justify-center">
            <button
              onClick={() => navigate('/dashboard')}
              className="bg-green-700 text-white px-6 py-3 rounded hover:bg-green-800 dark:hover:bg-green-600 transition"
            >
              Go to Dashboard
            </button>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-8">
          <div className="flex items-center gap-4 bg-white dark:bg-gray-800 p-4 rounded shadow-md">
            <FaChartLine className="text-3xl text-green-700 dark:text-green-400" />
            <div>
              <h3 className="font-bold text-lg">Personalized Nutrition</h3>
              <p className="text-sm text-gray-700 dark:text-gray-300">
                Plans tailored to your calorie and macro goals.
              </p>
            </div>
          </div>

          <div className="flex items-center gap-4 bg-white dark:bg-gray-800 p-4 rounded shadow-md">
            <FaShoppingCart className="text-3xl text-green-700 dark:text-green-400" />
            <div>
              <h3 className="font-bold text-lg">Auto-Generated Shopping List</h3>
              <p className="text-sm text-gray-700 dark:text-gray-300">
                Get a full grocery list with every weekly plan.
              </p>
            </div>
          </div>
        </div>
      </div>

      <section className="mt-16 bg-white dark:bg-gray-800 p-8 rounded-lg shadow-md max-w-4xl mx-auto text-center">
        <h2 className="text-2xl font-bold mb-4 text-green-700 dark:text-green-400">Why use Meal Planner AI?</h2>
        <ul className="list-disc text-left list-inside text-gray-700 dark:text-gray-300 space-y-2">
          <li>⏱️ Save hours of planning every week</li>
          <li>📊 Automatically tracks your nutritional progress</li>
          <li>📆 Build healthy habits with consistent meal structure</li>
        </ul>
      </section>
    </div>
  );
}

export default HomePage;
