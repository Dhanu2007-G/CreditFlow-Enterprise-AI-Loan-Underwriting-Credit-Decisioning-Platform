import React, { useState } from 'react';
import {
  Box, Card, CardContent, Typography, TextField, Button,
  Grid, MenuItem, Alert, CircularProgress, Chip, Divider
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { submitLoanApplication } from '../services/api';

const employmentTypes = [
  { value: 'FULL_TIME', label: 'Full Time' },
  { value: 'PART_TIME', label: 'Part Time' },
  { value: 'SELF_EMPLOYED', label: 'Self Employed' },
  { value: 'CONTRACT', label: 'Contract' },
  { value: 'UNEMPLOYED', label: 'Unemployed' }
];

const loanPurposes = [
  'Home Renovation', 'Car Purchase', 'Debt Consolidation',
  'Business Expansion', 'Education', 'Medical', 'Wedding', 'Travel', 'Other'
];

const statusColors = {
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'error',
  CONDITIONAL_APPROVAL: 'info',
  UNDER_REVIEW: 'warning'
};

export default function LoanApplicationForm() {
  const [form, setForm] = useState({
    applicantName: '',
    email: '',
    loanAmount: '',
    loanPurpose: '',
    loanTermMonths: '',
    annualIncome: '',
    creditScore: '',
    monthlyDebt: '',
    employmentType: '',
    employmentYears: ''
  });

  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(null);
  const [error, setError] = useState(null);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      const payload = {
        ...form,
        loanAmount: parseFloat(form.loanAmount),
        loanTermMonths: parseInt(form.loanTermMonths),
        annualIncome: parseFloat(form.annualIncome),
        creditScore: parseInt(form.creditScore),
        monthlyDebt: parseFloat(form.monthlyDebt),
        employmentYears: parseInt(form.employmentYears)
      };

      const response = await submitLoanApplication(payload);
      setSuccess(response.data);
      setForm({
        applicantName: '', email: '', loanAmount: '',
        loanPurpose: '', loanTermMonths: '', annualIncome: '',
        creditScore: '', monthlyDebt: '', employmentType: '',
        employmentYears: ''
      });
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit application');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Typography variant="h4" fontWeight={700} gutterBottom color="primary">
        Loan Application
      </Typography>
      <Typography variant="body1" color="text.secondary" mb={3}>
        Complete the form below to apply for a loan. Our AI-powered system
        will analyze your application instantly.
      </Typography>

      {success && (
        <Alert
          severity="success"
          icon={<CheckCircleIcon />}
          sx={{ mb: 3 }}
          action={
            <Chip
              label={success.status}
              color={statusColors[success.status] || 'default'}
              size="small"
            />
          }
        >
          <Typography fontWeight={600}>Application Submitted Successfully!</Typography>
          <Typography variant="body2">
            Application ID: <strong>{success.id}</strong>
          </Typography>
          <Typography variant="body2">{success.message}</Typography>
        </Alert>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>
      )}

      <Card elevation={2}>
        <CardContent sx={{ p: 4 }}>
          <form onSubmit={handleSubmit}>
            <Typography variant="h6" fontWeight={600} mb={2}>
              Personal Information
            </Typography>
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth label="Full Name" name="applicantName"
                  value={form.applicantName} onChange={handleChange}
                  required variant="outlined"
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth label="Email Address" name="email"
                  type="email" value={form.email} onChange={handleChange}
                  required variant="outlined"
                />
              </Grid>
            </Grid>

            <Divider sx={{ my: 3 }} />
            <Typography variant="h6" fontWeight={600} mb={2}>
              Loan Details
            </Typography>
            <Grid container spacing={3}>
              <Grid item xs={12} md={4}>
                <TextField
                  fullWidth label="Loan Amount ($)" name="loanAmount"
                  type="number" value={form.loanAmount} onChange={handleChange}
                  required inputProps={{ min: 1000, max: 1000000 }}
                />
              </Grid>
              <Grid item xs={12} md={4}>
                <TextField
                  fullWidth label="Loan Purpose" name="loanPurpose"
                  select value={form.loanPurpose} onChange={handleChange} required
                >
                  {loanPurposes.map(p => (
                    <MenuItem key={p} value={p}>{p}</MenuItem>
                  ))}
                </TextField>
              </Grid>
              <Grid item xs={12} md={4}>
                <TextField
                  fullWidth label="Loan Term (months)" name="loanTermMonths"
                  type="number" value={form.loanTermMonths} onChange={handleChange}
                  required inputProps={{ min: 12, max: 360 }}
                />
              </Grid>
            </Grid>

            <Divider sx={{ my: 3 }} />
            <Typography variant="h6" fontWeight={600} mb={2}>
              Financial Information
            </Typography>
            <Grid container spacing={3}>
              <Grid item xs={12} md={4}>
                <TextField
                  fullWidth label="Annual Income ($)" name="annualIncome"
                  type="number" value={form.annualIncome} onChange={handleChange}
                  required inputProps={{ min: 0 }}
                />
              </Grid>
              <Grid item xs={12} md={4}>
                <TextField
                  fullWidth label="Credit Score" name="creditScore"
                  type="number" value={form.creditScore} onChange={handleChange}
                  required inputProps={{ min: 300, max: 850 }}
                />
              </Grid>
              <Grid item xs={12} md={4}>
                <TextField
                  fullWidth label="Monthly Debt ($)" name="monthlyDebt"
                  type="number" value={form.monthlyDebt} onChange={handleChange}
                  required inputProps={{ min: 0 }}
                />
              </Grid>
            </Grid>

            <Divider sx={{ my: 3 }} />
            <Typography variant="h6" fontWeight={600} mb={2}>
              Employment Information
            </Typography>
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth label="Employment Type" name="employmentType"
                  select value={form.employmentType} onChange={handleChange} required
                >
                  {employmentTypes.map(t => (
                    <MenuItem key={t.value} value={t.value}>{t.label}</MenuItem>
                  ))}
                </TextField>
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth label="Years Employed" name="employmentYears"
                  type="number" value={form.employmentYears} onChange={handleChange}
                  required inputProps={{ min: 0 }}
                />
              </Grid>
            </Grid>

            <Box mt={4}>
              <Button
                type="submit" variant="contained" size="large"
                disabled={loading}
                sx={{ px: 6, py: 1.5, backgroundColor: '#1a237e' }}
              >
                {loading ? <CircularProgress size={24} color="inherit" /> : 'Submit Application'}
              </Button>
            </Box>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}