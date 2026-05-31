/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Custom UDOM Color Scheme
        blue: {
          50: '#EEF4FF',   // Card/background alt
          100: '#DDEAFC',
          200: '#B8D4F8',
          300: '#89B7F0',
          400: '#6A9DD4',
          500: '#4F7CAC',   // Primary blue
          600: '#4F7CAC',   // Primary blue (alias)
          700: '#3E6791',   // Hover blue
          800: '#2D4F73',
          900: '#1C3755',
        },
        // UDOM namespace
        udom: {
          blue: {
            50: '#EEF4FF',
            100: '#DDEAFC',
            200: '#B8D4F8',
            300: '#89B7F0',
            400: '#6A9DD4',
            500: '#4F7CAC',
            600: '#4F7CAC',
            700: '#3E6791',
            800: '#2D4F73',
            900: '#1C3755',
          },
          gold: {
            50: '#fef9f0',
            100: '#fce8cc',
            200: '#f9d699',
            300: '#f5c466',
            400: '#f3b233',
            500: '#f1a30f',
            600: '#d9900d',
            700: '#b9770a',
            800: '#8d5a08',
            900: '#613c05',
          },
          light: {
            50: '#F8FAFC',
            100: '#EEF4FF',
            200: '#E2ECF8',
            300: '#D7E3F4',  // Border
            400: '#B8C9DE',
            500: '#9AB0C8',
          },
          dark: {
            50: '#F8FAFC',
            100: '#E5E7EB',
            200: '#D1D5DB',
            300: '#9CA3AF',
            400: '#6B7280',
            500: '#4B5563',
            600: '#374151',
            700: '#1F2937',  // Dark text
            800: '#1A202C',
            900: '#111827',
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
        // Primary color alias
        primary: {
          50: '#EEF4FF',
          100: '#DDEAFC',
          200: '#B8D4F8',
          300: '#89B7F0',
          400: '#6A9DD4',
          500: '#4F7CAC',
          600: '#4F7CAC',
          700: '#3E6791',
          800: '#2D4F73',
          900: '#1C3755',
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
