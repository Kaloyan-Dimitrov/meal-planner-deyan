export default function ShoppingListModal({ open, onClose, list }) {
  if (!open) return null;

  const hasItems = Array.isArray(list) && list.length > 0;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50 text-black dark:text-white">
      <div className="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-lg max-w-md w-full">
        <h2 className="text-xl font-semibold mb-4">Shopping List</h2>

        {!Array.isArray(list) ? (
          <p className="text-red-600 dark:text-red-400 text-sm">Error loading shopping list.</p>
        ) : !hasItems ? (
          <p className="text-gray-600 dark:text-gray-300">No items found.</p>
        ) : (
          <ul className="space-y-2 max-h-96 overflow-y-auto pr-2">
            {list.map((item, index) => (
              <li key={index}>
                {item.name} — {item.quantityText}
              </li>
            ))}
          </ul>
        )}

        <button
          onClick={onClose}
          className="mt-6 bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded"
        >
          Close
        </button>
      </div>
    </div>
  );
}
