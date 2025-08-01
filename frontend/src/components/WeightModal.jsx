import { useState } from 'react';
import { toast } from 'react-toastify';
import { apiFetch } from '../utils/auth';

export default function WeightModal({ open, onClose, userId, authHeader, onSuccess }) {
    const [weight, setWeight] = useState('');

    if (!open) return null;

    const save = async () => {
        if (!weight || Number(weight) <= 0) return toast.error('Enter a valid weight');

        const res = await apiFetch(`/api/users/${userId}/weight`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', ...authHeader },
            body: JSON.stringify({ weight: Number(weight) }),
        });

        if (!res.ok) {
            let errorMessage = 'Error logging weight';
            try {
                const data = await res.clone().json();
                errorMessage = data.message || errorMessage;
            } catch (e) {
                const text = await res.text();
                if (text) errorMessage = text;
            }

            toast.error(errorMessage);
            return;
        }

        const data = await res.json();
        toast.success('✅ Weight logged!');
        setWeight('');
        onClose();
        onSuccess?.(data);
    };



    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
            <div className="bg-white dark:bg-gray-800 text-gray-900 dark:text-white p-6 rounded-lg w-full max-w-sm shadow-lg">
                <h2 className="text-xl font-semibold mb-4">Update Today&rsquo;s Weight</h2>

                <input
                    type="number"
                    min="0.1"
                    value={weight}
                    onChange={(e) => setWeight(e.target.value)}
                    placeholder="kg"
                    className="w-full border px-3 py-2 rounded mb-4 bg-white dark:bg-gray-700 text-gray-800 dark:text-white placeholder-gray-400 dark:placeholder-gray-500"
                />

                <div className="flex justify-end space-x-3">
                    <button
                        onClick={onClose}
                        className="bg-gray-300 dark:bg-gray-600 hover:bg-gray-400 dark:hover:bg-gray-500 text-black dark:text-white px-4 py-2 rounded"
                    >
                        Cancel
                    </button>
                    <button
                        onClick={save}
                        className="bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded"
                    >
                        Save
                    </button>
                </div>
            </div>
        </div>
    );
}
