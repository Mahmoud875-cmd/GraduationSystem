import defaultTheme from 'tailwindcss/defaultTheme';
import forms from '@tailwindcss/forms';

/** @type {import('tailwindcss').Config} */
export default {
    content: [
        './vendor/laravel/framework/src/Illuminate/Pagination/resources/views/*.blade.php',
        './storage/framework/views/*.php',
        './resources/views/**/*.blade.php',
    ],

    theme: {
        extend: {
            fontFamily: {
                sans: ['Vazirmatn', ...defaultTheme.fontFamily.sans],
            },
            colors: {
                // Material 3 tokens matching the Android app (Stitch design)
                primary: '#3525CD',
                'on-primary': '#FFFFFF',
                'primary-container': '#4F46E5',
                'on-primary-container': '#FFFFFF',
                'primary-fixed': '#E2DFFF',
                'primary-fixed-dim': '#C3C0FF',
                'on-primary-fixed': '#0F0069',
                secondary: '#555F6F',
                'secondary-container': '#D6E0F3',
                'on-secondary-container': '#596373',
                tertiary: '#46484A',
                error: '#BA1A1A',
                'error-container': '#FFDAD6',
                background: '#F9F9F9',
                surface: '#F9F9F9',
                'surface-container-lowest': '#FFFFFF',
                'surface-container-low': '#F3F3F4',
                'surface-container': '#EEEEEE',
                'surface-container-high': '#E8E8E8',
                'surface-container-highest': '#E2E2E2',
                'on-surface': '#1A1C1C',
                'on-surface-variant': '#464555',
                outline: '#777587',
                'outline-variant': '#C7C4D8',
                // Status colors
                'status-pending-bg': '#FEF3C7',
                'status-pending-fg': '#92400E',
                'status-approved-bg': '#DCFCE7',
                'status-approved-fg': '#166534',
                'status-inprogress-bg': '#DBEAFE',
                'status-inprogress-fg': '#1E40AF',
                'status-rejected-bg': '#FEE2E2',
                'status-rejected-fg': '#991B1B',
                // Accent colors from app
                emerald: { 50: '#ECFDF5', 100: '#D1FAE5', 600: '#059669', 800: '#065F46' },
                amber: { 50: '#FFFBEB', 100: '#FEF3C7', 800: '#92400E' },
                indigo: { 50: '#EEF2FF', 100: '#E0E7FF', 600: '#4F46E5', 700: '#4338CA', 800: '#3730A3' },
            },
            borderRadius: {
                '2xl': '1rem',
                '3xl': '1.5rem',
                'full': '9999px',
            },
        },
    },

    plugins: [forms],
};
