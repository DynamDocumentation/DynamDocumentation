/**
 * Auth utility functions for managing authentication
 */

/**
 * Get the authentication token from session storage
 * @returns {string|null} The auth token or null if not found
 */
export const getAuthToken = () => {
  return sessionStorage.getItem('authToken');
};

/**
 * Check if the user is logged in
 * @returns {boolean} True if logged in, false otherwise
 */
export const isAuthenticated = () => {
  return !!getAuthToken();
};

/**
 * Clear authentication data from session storage (logout)
 */
export const clearAuthData = () => {
  sessionStorage.removeItem('authToken');
};

/**
 * Get authorization headers for API requests
 * @returns {Object} Headers object with Authorization header if authenticated
 */
export const getAuthHeaders = () => {
  const token = getAuthToken();
  if (token) {
    return { 
      'Authorization': `Bearer ${token}`,
      'X-Auth-Token': token
    };
  }
  return {};
};
