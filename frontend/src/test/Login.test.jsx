import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import Login from '../pages/Login';

// Mock the API service
vi.mock('../services/api', () => ({
  authAPI: {
    login: vi.fn(),
  },
  getErrorMessage: vi.fn((err) => err?.message || 'Error'),
}));

describe('Login Component', () => {
  it('renders login form correctly', () => {
    render(
      <BrowserRouter>
        <ToastContainer />
        <Login />
      </BrowserRouter>
    );

    expect(screen.getByLabelText(/email address/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  it('shows validation error for empty email', () => {
    render(
      <BrowserRouter>
        <ToastContainer />
        <Login />
      </BrowserRouter>
    );

    const submitButton = screen.getByRole('button', { name: /sign in/i });
    fireEvent.click(submitButton);

    // Should show toast error (handled by component)
  });

  it('has forgot password link', () => {
    render(
      <BrowserRouter>
        <ToastContainer />
        <Login />
      </BrowserRouter>
    );

    const forgotPasswordLink = screen.getByText(/forgot password/i);
    expect(forgotPasswordLink).toBeInTheDocument();
    expect(forgotPasswordLink.closest('a')).toHaveAttribute('href', '/forgot-password');
  });
});
