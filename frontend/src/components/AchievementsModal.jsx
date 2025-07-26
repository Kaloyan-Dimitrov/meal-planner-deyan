import { useEffect } from 'react';

function AchievementsModal({ open, onClose, achievements }) {
  useEffect(() => {
    if (!open) return;

    const original = document.body.style.overflow;
    document.body.style.overflow = 'hidden';   // This locks the scroll in the back while the modal is opened

    return () => {
      document.body.style.overflow = original; // restore on unmount
    };
  }, [open]);
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div className="bg-white w-full max-w-md p-6 rounded-lg shadow-lg">
        <h2 className="text-xl font-semibold mb-4 text-center">Achievements</h2>

        <div className="space-y-4 max-h-[60vh] overflow-y-auto">
          {achievements.map((a) => (
            <div key={a.id} className="flex flex-col">
              <span
                className={
                  a.unlocked ? 'text-green-700 font-medium' : 'text-gray-800'
                }
              >
                {a.name}
              </span>
              <span className="text-xs text-gray-500">{a.description}</span>

              {a.unlocked ? (
                <span className="text-xs text-green-600 mt-1">Completed 🎉</span>
              ) : (
                <div className="w-full bg-gray-200 h-2 rounded-full mt-1">
                  <div
                    className="bg-blue-500 h-full rounded-full"
                    style={{
                      width: `${Math.min(
                        100,
                        Math.round((a.progress / a.target) * 100)
                      )}%`,
                    }}
                  />
                </div>
              )}
            </div>
          ))}
        </div>

        <button
          onClick={onClose}
          className="block mt-6 mx-auto bg-gray-300 hover:bg-gray-400 px-4 py-2 rounded"
        >
          Close
        </button>
      </div>
    </div>
  );
}
export default AchievementsModal;