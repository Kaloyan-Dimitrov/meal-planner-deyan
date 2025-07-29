import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';
import RecipeModal from '../components/RecipeModal'
import WeightModal from '../components/WeightModal';
import AchievementsModal from '../components/AchievementsModal';
import { toast } from 'react-toastify';
import { clearTokens } from '../utils/auth';
import { apiFetch } from '../utils/auth';
import { toggleTheme } from '../utils/theme';
import { Menu } from '@headlessui/react';
import { FaUserCircle, FaSun, FaMoon, FaSignOutAlt, FaTrophy, FaWeight, FaCog } from 'react-icons/fa';


// DashboardPage.jsx – parses backend response shape (meals array, actual macros)
export default function DashboardPage() {
  const navigate = useNavigate();

  /* ---------------- Auth ---------------- */
  const token = localStorage.getItem('jwt');
  const authHeader = token ? { Authorization: `Bearer ${token}` } : {};

  let userId = null;
  if (token) {
    try {
      const claims = jwtDecode(token);
      userId = claims.userId || claims.sub || claims.id || null;
    } catch (e) {
      console.warn('Failed to decode JWT', e);
    }
  }
  if (!userId) {
    return <Navigate to="/login" replace />;
  }

  /* ---------------- Static options ---------------- */
  const planLengths = [1, 7];
  const slots = ['Breakfast', 'Lunch', 'Dinner'];
  const slotLabel = (s) => s.charAt(0).toUpperCase() + s.slice(1);

  /* ---------------- State ---------------- */
  const [plans, setPlans] = useState([]);
  const [selectedPlanId, setSelectedPlanId] = useState(null);
  const [macros, setMacros] = useState({});
  const [mealPlan, setMealPlan] = useState([]);
  const [params, setParams] = useState({ targetKcal: 2000, proteinG: 150, carbG: 250, fatG: 70, days: 7 });
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [selectedRecipe, setSelectedRecipe] = useState(null);
  const [lockedTargets, setLockedTargets] = useState(null);
  const [showWeightModal, setShowWeightModal] = useState(false);
  const [achievements, setAchievements] = useState([]);
  const [showAchModal, setShowAchModal] = useState(false);


  /* ---------------- Helper: fetch JSON with auth ---------------- */
  const fetchJson = async (url, opts = {}) => {
    setError(null);
    setLoading(true);
    try {
      const res = await apiFetch(url, opts);
      const data = res.headers.get('content-type')?.includes('application/json') ? await res.json() : null;
      if (!res.ok) throw new Error(data.message || res.status);
      return data;
    } catch (e) {
      setError(e.message);
      return null;
    } finally {
      setLoading(false);
    }
  };

  const openRecipeModal = async (recipeId) => {
    const details = await fetchJson(`/api/recipes/${recipeId}`);
    if (details) setSelectedRecipe(details);
  };

  const loadAchievements = async () => {
    const data = await fetchJson(`/api/users/${userId}/achievements`);
    if (Array.isArray(data)) setAchievements(data);
  };
  const toastNewUnlocks = (ids = []) => {
    ids.forEach((id) => {
      const a = achievements.find((x) => x.id === id);
      if (a) toast.success(`🏅 Achievement unlocked: ${a.name}`);
    });
  };
  /* ---------------- Load plans list ---------------- */
  useEffect(() => {
    (async () => {
      const list = await fetchJson(`/api/users/${userId}/meal-plans`);
      if (Array.isArray(list)) {
        setPlans(list);
        if (list.length) setSelectedPlanId(list[0].id);
      }
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => { loadAchievements(); /* eslint-disable-next-line */ }, []);
  /* ---------------- Parse plan details ---------------- */
  const parsePlanDetails = (data) => {
    if (!data) return;

    /* macro summary */
    setMacros({
      calories: data.actualKcal ?? data.targetKcal,
      protein: data.actualProteinG ?? data.targetProteinG,
      carbs: data.actualCarbG ?? data.targetCarbG,
      fat: data.actualFatG ?? data.targetFatG,
    });

    setLockedTargets({
      targetKcal: data.targetKcal,
      proteinG: data.targetProteinG,
      carbG: data.targetCarbG,
      fatG: data.targetFatG,
    });
    setParams((prev) => ({
      ...prev,
      targetKcal: data.targetKcal ?? prev.targetKcal,
      proteinG: data.targetProteinG ?? prev.proteinG,
      carbG: data.targetCarbG ?? prev.carbG,
      fatG: data.targetFatG ?? prev.fatG,
      days: (data.meals ? new Set(data.meals.map(m => m.day)).size : prev.days) || prev.days
    }));

    /* group meals by day & slot – backend day is 0‑based */
    const grouped = {};

    (data.meals ?? []).forEach(({ day, mealSlot, recipe }) => {
      const rawIndex = typeof day === 'number'
        ? day
        : parseInt(day.match(/\d+/)?.[0] ?? '0', 10);

      if (!grouped[rawIndex]) grouped[rawIndex] = { day: rawIndex + 1, meals: {} }; // display as 1‑based

      grouped[rawIndex].meals[slotLabel(mealSlot)] = {
        id: recipe.id,
        title: recipe.title,
        readyInMinutes: recipe.prepTime,
        servings: recipe.servings,
        sourceUrl: recipe.sourceUrl,
      };
    });

    setMealPlan(
      Object.values(grouped).sort((a, b) => a.day - b.day)
    );
    console.log('mealPlan →', grouped);
  };
  const handleLogout = async () => {
    // grab the refresh-token cookie (may be undefined if httpOnly mode)
    const rt = document.cookie
      .split('; ')
      .find(c => c.startsWith('rt='))?.split('=')[1];

    // tell the backend to invalidate it (safe even if rt === undefined)
    try {
      await apiFetch('/auth/logout', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: rt })
      });
    } catch (e) {
      console.warn('Logout call failed:', e);
    }

    clearTokens();           // wipes localStorage + cookie
    navigate('/login');      // send user to login page
  };
  /* ---------------- Load selected plan details ---------------- */
  useEffect(() => {
    if (!selectedPlanId) return;
    (async () => {
      const details = await fetchJson(`/api/users/${userId}/meal-plans/${selectedPlanId}`);
      parsePlanDetails(details);
    })();
  }, [selectedPlanId]);

  /* ---------------- Create & regenerate ---------------- */
  const handleGenerate = async () => {
    const created = await fetchJson(`/api/users/${userId}/meal-plans`, {
      method: 'POST',
      body: JSON.stringify(params),
    });
    if (created?.id) setSelectedPlanId(created.id);
  };
  const handleRegenerate = async () => {
    if (!selectedPlanId) return;
    const refreshed = await fetchJson(`/api/users/${userId}/meal-plans/${selectedPlanId}/regenerate`, {
      method: 'POST',
    });
    parsePlanDetails(refreshed);
  };
  /* ---------------- Render ---------------- */
  return (
    <div className="relative min-h-screen bg-peach dark:bg-gray-900 text-gray-800 dark:text-white p-4">
      {/* Nav */}
      <header className="flex items-center justify-between mb-6">
        <div className="bg-white rounded shadow px-3 pt-2 pb-2 text-center">
          <h1 className="text-2xl font-bold text-gray-800">My Dashboard</h1>
        </div>
        <Menu as="div" className="relative inline-block text-left">
          <Menu.Button className="flex items-center space-x-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded">
            <FaUserCircle />
            <span>Account</span>
          </Menu.Button>

          <Menu.Items className="absolute right-0 mt-2 w-56 origin-top-right bg-white dark:bg-gray-800 divide-y divide-gray-100 rounded-md shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none z-50">
            <div className="px-1 py-1">
              <Menu.Item>
                {({ active }) => (
                  <button
                    onClick={() => setShowAchModal(true)}
                    className={`${active ? 'bg-blue-500 text-white' : 'text-gray-800 dark:text-white'
                      } group flex w-full items-center px-4 py-2 text-sm`}
                  >
                    <FaTrophy className="mr-2" /> Achievements
                  </button>
                )}
              </Menu.Item>
              <Menu.Item>
                {({ active }) => (
                  <button
                    onClick={() => setShowWeightModal(true)}
                    className={`${active ? 'bg-blue-500 text-white' : 'text-gray-800 dark:text-white'
                      } group flex w-full items-center px-4 py-2 text-sm`}
                  >
                    <FaWeight className="mr-2" /> Log Weight
                  </button>
                )}
              </Menu.Item>
              <Menu.Item>
                {({ active }) => (
                  <button
                    onClick={() => navigate("/account")}
                    className={`${active ? 'bg-blue-500 text-white' : 'text-gray-800 dark:text-white'
                      } group flex w-full items-center px-4 py-2 text-sm`}
                  >
                    <FaCog className="mr-2" /> Account Settings
                  </button>
                )}
              </Menu.Item>
              <Menu.Item>
                {({ active }) => (
                  <button
                    onClick={toggleTheme}
                    className={`${active ? 'bg-blue-500 text-white' : 'text-gray-800 dark:text-white'
                      } group flex w-full items-center px-4 py-2 text-sm`}
                  >
                    <FaSun className="mr-2 dark:hidden" />
                    <FaMoon className="mr-2 hidden dark:inline" />
                    Toggle Theme
                  </button>
                )}
              </Menu.Item>
              <Menu.Item>
                {({ active }) => (
                  <button
                    onClick={handleLogout}
                    className={`${active ? 'bg-red-500 text-white' : 'text-gray-800 dark:text-white'
                      } group flex w-full items-center px-4 py-2 text-sm`}
                  >
                    <FaSignOutAlt className="mr-2" /> Logout
                  </button>
                )}
              </Menu.Item>
            </div>
          </Menu.Items>
        </Menu>

      </header >

      {error && <div className="bg-red-100 text-red-800 p-3 rounded mb-4">Error: {error}</div>
      }
      {loading && <div className="text-gray-600 mb-4">Loading...</div>}

      <main className="space-y-8">
        {/* Plan selector */}
        <section className="flex items-center space-x-4">
          <label className="flex items-center">
            <span className="mr-2text-gray-800">Saved Plans:</span>
            <select value={selectedPlanId || ''} onChange={e => setSelectedPlanId(e.target.value)} className="border rounded px-2 py-1 text-gray-800">
              {plans.map(p => <option key={p.id} value={p.id}>{p.name || `Plan ${p.id}`}</option>)}
            </select>
          </label>
        </section>

        {/* Targets & controls */}
        <section className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <h2 className="text-xl font-semibold mb-4">Macro Targets</h2>
            {['targetKcal', 'proteinG', 'carbG', 'fatG'].map(k => (
              <div key={k} className="flex items-center mb-2 ">
                <label className="w-24 capitalize">{k.replace('G', '')}:</label>
                <input type="number" value={params[k]} onChange={e => setParams(prev => ({ ...prev, [k]: Number(e.target.value) }))} className="border rounded px-2 py-1 flex-grow text-gray-800"/>
                <span className="ml-2">{k === 'targetKcal' ? 'kcal' : 'g'}</span>
              </div>
            ))}
            <p className="text-xs text-gray-500 mt-2">
              ⚠️&nbsp;Macros targets are for feedback only — SpoonacularAPI may return meal plans that don’t match them exactly.
            </p>
            <div className="flex items-center mt-4">
              <label className="mr-2">Plan Duration:</label>
              <select value={params.days} onChange={e => setParams(prev => ({ ...prev, days: Number(e.target.value) }))} className="border rounded px-2 py-1 text-gray-800">
                {planLengths.map(d => <option key={d} value={d}>{d} day{d > 1 ? 's' : ''}</option>)}
              </select>
            </div>
            <div className="mt-4 flex space-x-4">
              <button onClick={handleGenerate} className="bg-green-700 hover:bg-green-800 text-white px-4 py-2 rounded">Generate Plan</button>
              <button onClick={handleRegenerate} className="bg-yellow-500 text-white px-4 py-2 rounded hover:bg-yellow-600">Regenerate</button>
            </div>
          </div>

          {/* Macro summary */}
          <div className="grid grid-cols-2 grid-rows-2 gap-4 ">
            {['calories', 'protein', 'carbs', 'fat'].map(key => {
              const targetValue = lockedTargets?.[key === 'calories' ? 'targetKcal' : key === 'protein' ? 'proteinG' : key === 'carbs' ? 'carbG' : 'fatG'];
              const over = macros[key] - targetValue;
              return (
                <div key={key}
                  className={`p-4 rounded-lg shadow flex flex-col justify-items-center
                      ${over < -5 ? 'bg-red-100 text-red-700'
                      : over > 5 ? 'bg-green-100 text-green-700'
                        : 'bg-white'}`}>
                  <span className="text-sm capitalize text-gray-800">{key}</span>
                  <span className="text-2xl font-bold text-gray-800">{macros[key] ?? '--'}</span>
                </div>
              );
            })}
          </div>
        </section>

        {/* Meal plan grid */}
        <section>
          <h2 className="text-xl font-semibold mb-4">Meal Plan</h2>
          <div className="overflow-x-auto">
            <table className="min-w-full bg-white dark:bg-gray-800 text-gray-800 dark:text-white rounded-lg shadow">
              <thead>
                <tr>
                  <th className="px-4 py-2 sticky left-0 bg-white dark:bg-gray-800 text-gray-800 dark:text-white">Day</th>
                  {slots.map(s => <th key={s} className="px-4 py-2">{s}</th>)}
                </tr>
              </thead>
              <tbody>
                {mealPlan.length === 0 ? (
                  <tr><td className="px-4 py-2" colSpan={slots.length + 1}>No data</td></tr>
                ) : (
                  mealPlan.map((d, i) => (
                    <tr key={i} className="border-t">
                      <td className="px-4 py-2 font-medium sticky left-0 bg-white dark:text-white dark:bg-gray-800">{d.day}</td>
                      {slots.map((s) => {
                        const meal = d.meals[s];
                        return (
                          <td key={s} className="px-4 py-2">
                            {meal ? (
                              <button
                                className="text-blue-600 underline"
                                onClick={() => openRecipeModal(meal.id)}
                              >
                                {meal.title}
                              </button>
                            ) : (
                              '--'
                            )}
                          </td>
                        );
                      })}
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </section>
      </main>
      {
        selectedRecipe && (
          <RecipeModal
            recipe={selectedRecipe}
            onClose={() => setSelectedRecipe(null)}
          />
        )
      }
      {
        showWeightModal && (
          <WeightModal
            open={showWeightModal}
            onClose={() => setShowWeightModal(false)}
            userId={userId}
            authHeader={authHeader}
            onSuccess={(payload) => {
              if (payload.newAchievements?.length) {
                toastNewUnlocks(payload.newAchievements);
                loadAchievements();
              }
            }}
          />)
      }
      <AchievementsModal
        open={showAchModal}
        onClose={() => setShowAchModal(false)}
        achievements={achievements}
      />

    </div >
  );
}
