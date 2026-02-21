/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // UDOM Brand Colors
        udom: {
          blue: {
            50: '#e6f0ff',
            100: '#b3d1ff',
            200: '#80b3ff',
            300: '#4d94ff',
            400: '#1a75ff',
            500: '#0047AB', // Primary UDOM Blue
            600: '#003d91',
            700: '#003377',
            800: '#00295d',
            900: '#001f43',
          },
          gold: {
            50: '#fff9e6',
            100: '#fff0b3',
            200: '#ffe680',
            300: '#ffdd4d',
            400: '#ffd31a',
            500: '#FFD700', // UDOM Gold
            600: '#e6c200',
            700: '#ccad00',
            800: '#b39900',
            900: '#998400',
          },
          green: {
            500: '#10B981',
            600: '#059669',
          },
          red: {
            500: '#EF4444',
            600: '#DC2626',
          }
        },
        // Alias primary to UDOM blue
        primary: {
          50: '#e6f0ff',
          100: '#b3d1ff',
          200: '#80b3ff',
          300: '#4d94ff',
          400: '#1a75ff',
          500: '#0047AB',
          600: '#003d91',
          700: '#003377',
          800: '#00295d',
          900: '#001f43',
        },
        secondary: {
          50: '#fff9e6',
          100: '#fff0b3',
          200: '#ffe680',
          300: '#ffdd4d',
          400: '#ffd31a',
          500: '#FFD700',
          600: '#e6c200',
          700: '#ccad00',
          800: '#b39900',
          900: '#998400',
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
