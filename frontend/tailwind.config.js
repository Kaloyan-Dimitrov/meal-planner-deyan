/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        peach: "#f7ccba",
        paleblue : "#96e7dc"
      },
    },
  },
  plugins: [],
}