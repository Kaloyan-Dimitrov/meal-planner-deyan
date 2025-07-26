import { useNavigate } from 'react-router-dom';
import { FaAppleAlt, FaChartLine, FaShoppingCart } from 'react-icons/fa';
import { FaBowlFood } from "react-icons/fa6";

function HomePage() {
  const navigate = useNavigate();

  return (
    // <div className="min-h-screen flex flex-col items-center justify-center text-gray-800 bg-peach">
    //   <header className="flex flex-col items-center text-center">
    //     <img
    //       src="/logo.svg"
    //       alt="Meal Planner AI Logo"
    //       className="w-16 h-16 mb-4"
    //     />
    //   <h1 className="text-white text-4xl font-bold mb-4 text-center">Meal Planner AI 🍽️</h1>
    //   <p className="text-white text-lg text-center max-w-xl mb-8">
    //     Your personal AI-powered meal planner. Generate weekly plans, track nutrition, and auto-create shopping lists — all tailored to your goals.
    //   </p>
    //   <div className="flex gap-4">
    //     <button
    //       onClick={() => navigate('/login')}
    //       className="bg-paleblue px-6 py-3 rounded hover:bg-blue-700 transition"
    //     >
    //       Login
    //     </button>
    //     <button
    //       onClick={() => navigate('/register')}
    //       className="bg-paleblue px-6 py-3 rounded hover:bg-blue-700 transition"
    //     >
    //       Register
    //     </button>
    //     <button
    //       onClick={() => navigate('/dashboard')}
    //       className="bg-paleblue px-6 py-3 rounded hover:bg-blue-700 transition"
    //     >
    //       Go To Dashboard
    //     </button>
    //   </div>
    // </div>
    <div className="min-h-screen bg-peach text-gray-800 px-6 py-8 flex flex-col">
      <header className="flex flex-col items-center text-center">
        <FaBowlFood className="text-5xl text-green-700" />
        <h1 className="text-white text-4xl md:text-5xl font-bold mb-4">
          Meal Planner 🍽️
        </h1>
        <p className="text-white text-lg md:text-xl max-w-xl">
          Generate weekly meal plans, track macros, and get shopping lists – all powered by AI.
        </p>
      </header>

      <div className="w-full max-w-4xl mx-auto">
        <div className="grid grid-cols-2 gap-8 mb-10 py-8">
          <div className="flex justify-center">
            <button
              onClick={() => navigate('/login')}
              className="bg-green-700 text-white px-6 py-3 rounded hover:bg-blue-700 transition"
            >
              Get Started
            </button>
          </div>
          <div className="flex justify-center">
            <button
              onClick={() => navigate('/dashboard')}
              className="bg-green-700 text-white px-6 py-3 rounded hover:bg-blue-700 transition"
            >
              Go to Dashboard
            </button>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-8">
          <div className="flex items-center gap-4 bg-white p-4 rounded shadow-md">
            <FaChartLine className="text-3xl text-green-700" />
            <div>
              <h3 className="font-bold text-lg">Personalized Nutrition</h3>
              <p className="text-sm">Plans tailored to your calorie and macro goals.</p>
            </div>
          </div>

          <div className="flex items-center gap-4 bg-white p-4 rounded shadow-md">
            <FaShoppingCart className="text-3xl text-green-700" />
            <div>
              <h3 className="font-bold text-lg">Auto-Generated Shopping List</h3>
              <p className="text-sm">Get a full grocery list with every weekly plan.</p>
            </div>
          </div>
        </div>

      </div>


      {/* Why Use the App */}
      <section className="mt-16 bg-white p-8 rounded-lg shadow-md max-w-4xl mx-auto text-center">
        <h2 className="text-2xl font-bold mb-4 text-green-700">Why use Meal Planner AI?</h2>
        <ul className="list-disc text-left list-inside text-gray-700 space-y-2">
          <li>🧠 Smart AI-suggested recipes tailored to your fitness goals</li>
          <li>⏱️ Save hours of planning every week</li>
          <li>📊 Automatically tracks your nutritional progress</li>
          <li>📆 Build healthy habits with consistent meal structure</li>
        </ul>
      </section>
    </div>
  );
}

export default HomePage;
