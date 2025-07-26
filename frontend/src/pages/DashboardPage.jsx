import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';

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
    navigate('/login');
    return null;
  }

  /* ---------------- Static options ---------------- */
  const planLengths = [1,7];
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

  /* ---------------- Helper: fetch JSON with auth ---------------- */
  const fetchJson = async (url, opts = {}) => {
    setError(null);
    setLoading(true);
    try {
      const res = await fetch(url, {
        headers: { 'Content-Type': 'application/json', ...authHeader },
        ...opts,
      });
      if (res.status === 403) {
        navigate('/login');
        return null;
      }
      const ct = res.headers.get('content-type') || '';
      if (!ct.includes('application/json')) throw new Error(`Expected JSON, got ${ct}`);
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || `${res.status}`);
      return data;
    } catch (e) {
      console.error(e);
      setError(e.message);
      return null;
    } finally {
      setLoading(false);
    }
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

    /* group meals by day & slot – backend day is 0‑based */
    const grouped = {};

    (data.meals ?? []).forEach(({ day, mealSlot, recipe }) => {
      // "Day 0" → 0  ,  0 → 0
      const rawIndex = typeof day === 'number'
        ? day
        : parseInt(day.match(/\d+/)?.[0] ?? '0', 10);

      if (!grouped[rawIndex]) grouped[rawIndex] = { day: rawIndex + 1, meals: {} }; // display as 1‑based

      grouped[rawIndex].meals[slotLabel(mealSlot)] = recipe.title;
    });

    setMealPlan(
      Object.values(grouped).sort((a, b) => a.day - b.day)
    );
  };

  /* ---------------- Load selected plan details ---------------- */
  useEffect(() => {
    if (!selectedPlanId) return;
    (async () => {
      const details = await fetchJson(`/api/users/${userId}/meal-plans/${selectedPlanId}`);
      parsePlanDetails(details);
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
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
    <div className="min-h-screen bg-peach text-gray-800 p-4">
      {/* Nav */}
      <header className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Dashboard</h1>
        <button onClick={() => { localStorage.removeItem('jwt'); navigate('/login'); }} className="bg-red-500 text-white px-4 py-2 rounded">Logout</button>
      </header>

      {error && <div className="bg-red-100 text-red-800 p-3 rounded mb-4">Error: {error}</div>}
      {loading && <div className="text-gray-600 mb-4">Loading...</div>}

      <main className="space-y-8">
        {/* Plan selector */}
        <section className="flex items-center space-x-4">
          <label className="flex items-center">
            <span className="mr-2">Saved Plans:</span>
            <select value={selectedPlanId || ''} onChange={e => setSelectedPlanId(e.target.value)} className="border rounded px-2 py-1">
              {plans.map(p => <option key={p.id} value={p.id}>{p.name || `Plan ${p.id}`}</option>)}
            </select>
          </label>
        </section>

        {/* Targets & controls */}
        <section className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <h2 className="text-xl font-semibold mb-4">Macro Targets</h2>
            {['targetKcal', 'proteinG', 'carbG', 'fatG'].map(k => (
              <div key={k} className="flex items-center mb-2">
                <label className="w-24 capitalize">{k.replace('G', '')}:</label>
                <input type="number" value={params[k]} onChange={e => setParams(prev => ({ ...prev, [k]: Number(e.target.value) }))} className="border rounded px-2 py-1 flex-grow" />
                <span className="ml-2">{k === 'targetKcal' ? 'kcal' : 'g'}</span>
              </div>
            ))}
            <div className="flex items-center mt-4">
              <label className="mr-2">Plan Duration:</label>
              <select value={params.days} onChange={e => setParams(prev => ({ ...prev, days: Number(e.target.value) }))} className="border rounded px-2 py-1">
                {planLengths.map(d => <option key={d} value={d}>{d} day{d > 1 ? 's' : ''}</option>)}
              </select>
            </div>
            <div className="mt-4 flex space-x-4">
              <button onClick={handleGenerate} className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600">Generate Plan</button>
              <button onClick={handleRegenerate} className="bg-yellow-500 text-white px-4 py-2 rounded hover:bg-yellow-600">Regenerate</button>
            </div>
          </div>

          {/* Macro summary */}
          <div className="grid grid-cols-2 grid-rows-2 gap-4">
            {['calories', 'protein', 'carbs', 'fat'].map(key => {
              const over = macros[key] - params[
                key === 'calories' ? 'targetKcal' : key === 'carbs' ? 'carbG' : key === 'protein' ? 'proteinG' : 'fatG'
              ];
              return (
                <div key={key}
                  className={`p-4 rounded-lg shadow flex flex-col justify-items-center
                      ${over < -5 ? 'bg-red-100 text-red-700'
                      : over > 5 ? 'bg-green-100 text-green-700'
                        : 'bg-white'}`}>
                  <span className="text-sm capitalize">{key}</span>
                  <span className="text-2xl font-bold">{macros[key] ?? '--'}</span>
                </div>
              );
            })}
          </div>
        </section>

        {/* Meal plan grid */}
        <section>
          <h2 className="text-xl font-semibold mb-4">Meal Plan</h2>
          <div className="overflow-x-auto">
            <table className="min-w-full bg-white rounded-lg shadow">
              <thead>
                <tr>
                  <th className="px-4 py-2 sticky left-0 bg-white">Day</th>
                  {slots.map(s => <th key={s} className="px-4 py-2">{s}</th>)}
                </tr>
              </thead>
              <tbody>
                {mealPlan.length === 0 ? (
                  <tr><td className="px-4 py-2" colSpan={slots.length + 1}>No data</td></tr>
                ) : (
                  mealPlan.map((d, i) => (
                    <tr key={i} className="border-t">
                      <td className="px-4 py-2 font-medium sticky left-0 bg-white">{d.day}</td>
                      {slots.map(s => <td key={s} className="px-4 py-2">{d.meals[s] ?? '--'}</td>)}
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </section>
      </main>
    </div>
  );
}
