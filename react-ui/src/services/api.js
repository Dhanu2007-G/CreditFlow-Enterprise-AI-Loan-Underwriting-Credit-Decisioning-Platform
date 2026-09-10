import axios from 'axios';

const API_BASE = 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' }
});

// Loan Applications
export const submitLoanApplication = (data) =>
  api.post('/api/v1/loans', data);

export const getLoanById = (id) =>
  api.get(`/api/v1/loans/${id}`);

export const getLoansByStatus = (status) =>
  api.get(`/api/v1/loans/status/${status}`);

// Underwriting Decisions
export const getAllDecisions = () =>
  api.get('/api/v1/underwriting/decisions');

export const getDecisionByApplicationId = (applicationId) =>
  api.get(`/api/v1/underwriting/decisions/application/${applicationId}`);

export const getDecisionsByOutcome = (outcome) =>
  api.get(`/api/v1/underwriting/decisions/outcome/${outcome}`);

// AI Analysis
export const analyzeWithAgent = (data) =>
  api.post('/api/v1/ai/analyze/agent', data);

export default api;