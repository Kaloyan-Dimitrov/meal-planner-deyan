function RecipeModal({ recipe, onClose }) {
    if (!recipe) {
        return null;
    }
    return (
    <div
      className="fixed inset-0 z-50 bg-black/60 flex items-center justify-center"
      onClick={onClose}
    >
      <div
        className="relative bg-white dark:bg-gray-800 text-gray-800 dark:text-white rounded-xl shadow-xl w-full max-w-lg max-h-[90vh] overflow-y-auto p-6"
        onClick={(e) => e.stopPropagation()}
      >
        <button
          className="absolute top-3 right-3 text-gray-500 dark:text-gray-300 hover:text-black dark:hover:text-white text-xl"
          onClick={onClose}
        >
          ×
        </button>

        <h2 className="text-2xl font-bold mb-1">{recipe.title}</h2>
        <p className="text-sm text-gray-600 dark:text-gray-300 mb-4">
          Prep time: {recipe.readyInMinutes} min • Servings: {recipe.servings}
        </p>

        <h3 className="font-semibold mb-2">Ingredients</h3>
        <ul className="list-disc list-inside space-y-1 mb-4">
          {recipe.extendedIngredients?.map((ing, i) => (
            <li key={i}>
              {ing.amount} {ing.unit} {ing.name}
            </li>
          ))}
        </ul>

        {recipe.sourceUrl ? (
          <a
            href={recipe.sourceUrl}
            target="_blank"
            rel="noreferrer"
            className="inline-block text-blue-600 dark:text-blue-400 underline"
          >
            Full instructions ↗
          </a>
        ) : (
          <span className="text-gray-500 dark:text-gray-400 italic">No instructions available</span>
        )}
      </div>
    </div>
  );
}
export default RecipeModal