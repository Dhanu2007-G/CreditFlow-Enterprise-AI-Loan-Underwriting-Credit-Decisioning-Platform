import React, { useState, useEffect } from 'react';
import {
  Box, Card, CardContent, Typography, Chip, Grid,
  Table, TableBody, TableCell, TableContainer, TableHead,
  TableRow, Paper, Alert, CircularProgress, Button,
  TextField, MenuItem
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import { getAllDecisions, getDecisionsByOutcome } from '../services/api';

const outcomeColors = {
  APPROVED: 'success',
  REJECTED: 'error',
  CONDITIONAL_APPROVAL: 'info',
  UNDER_REVIEW: 'warning'
};

const riskColors = {
  LOW: 'success',
  MEDIUM: 'warning',
  HIGH: 'error',
  CRITICAL: 'error'
};

export default function AdminDashboard() {
  const [decisions, setDecisions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState('ALL');

  const fetchDecisions = async (outcome) => {
    setLoading(true);
    setError(null);
    try {
      const res = outcome && outcome !== 'ALL'
        ? await getDecisionsByOutcome(outcome)
        : await getAllDecisions();
      setDecisions(res.data);
    } catch (err) {
      setError('Failed to load decisions');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDecisions();
  }, []);

  const handleFilterChange = (e) => {
    setFilter(e.target.value);
    fetchDecisions(e.target.value);
  };

  const stats = {
    total: decisions.length,
    approved: decisions.filter(d => d.outcome === 'APPROVED').length,
    rejected: decisions.filter(d => d.outcome === 'REJECTED').length,
    conditional: decisions.filter(d => d.outcome === 'CONDITIONAL_APPROVAL').length,
    avgRiskScore: decisions.length > 0
      ? (decisions.reduce((sum, d) => sum + d.riskScore, 0) / decisions.length).toFixed(1)
      : 0
  };

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h4" fontWeight={700} color="primary">
            Admin Dashboard
          </Typography>
          <Typography variant="body1" color="text.secondary">
            All underwriting decisions and risk assessments
          </Typography>
        </Box>
        <Button
          variant="outlined"
          startIcon={<RefreshIcon />}
          onClick={() => fetchDecisions(filter)}
        >
          Refresh
        </Button>
      </Box>

      {/* Stats Cards */}
      <Grid container spacing={2} mb={3}>
        {[
          { label: 'Total Applications', value: stats.total, color: '#1a237e' },
          { label: 'Approved', value: stats.approved, color: '#2e7d32' },
          { label: 'Rejected', value: stats.rejected, color: '#c62828' },
          { label: 'Conditional', value: stats.conditional, color: '#0277bd' },
          { label: 'Avg Risk Score', value: stats.avgRiskScore, color: '#e65100' }
        ].map(({ label, value, color }) => (
          <Grid item xs={6} md={2.4} key={label}>
            <Card elevation={2}>
              <CardContent sx={{ textAlign: 'center', py: 2 }}>
                <Typography variant="h4" fontWeight={700} sx={{ color }}>
                  {value}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {label}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Filter */}
      <Box display="flex" justifyContent="flex-end" mb={2}>
        <TextField
          select label="Filter by Outcome" value={filter}
          onChange={handleFilterChange} size="small" sx={{ width: 200 }}
        >
          <MenuItem value="ALL">All</MenuItem>
          <MenuItem value="APPROVED">Approved</MenuItem>
          <MenuItem value="REJECTED">Rejected</MenuItem>
          <MenuItem value="CONDITIONAL_APPROVAL">Conditional</MenuItem>
        </TextField>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {loading ? (
        <Box display="flex" justifyContent="center" py={6}>
          <CircularProgress />
        </Box>
      ) : (
        <TableContainer component={Paper} elevation={2}>
          <Table>
            <TableHead sx={{ backgroundColor: '#1a237e' }}>
              <TableRow>
                {['Applicant', 'Email', 'Loan Amount', 'Credit Score',
                  'Risk Score', 'Risk Level', 'DTI Ratio',
                  'Outcome', 'Interest Rate', 'Approved Amount'].map(h => (
                  <TableCell key={h} sx={{ color: 'white', fontWeight: 600 }}>
                    {h}
                  </TableCell>
                ))}
              </TableRow>
            </TableHead>
            <TableBody>
              {decisions.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={10} align="center">
                    <Typography color="text.secondary" py={3}>
                      No decisions found
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                decisions.map((d) => (
                  <TableRow key={d.id} hover>
                    <TableCell>{d.applicantName}</TableCell>
                    <TableCell>{d.email}</TableCell>
                    <TableCell>${d.loanAmount?.toLocaleString()}</TableCell>
                    <TableCell>{d.creditScore}</TableCell>
                    <TableCell>
                      <Chip
                        label={`${d.riskScore}/100`}
                        size="small"
                        color={d.riskScore >= 70 ? 'error' :
                               d.riskScore >= 40 ? 'warning' : 'success'}
                      />
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={d.riskLevel}
                        size="small"
                        color={riskColors[d.riskLevel] || 'default'}
                      />
                    </TableCell>
                    <TableCell>
                      {d.debtToIncomeRatio
                        ? `${(d.debtToIncomeRatio * 100).toFixed(1)}%`
                        : 'N/A'}
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={d.outcome}
                        size="small"
                        color={outcomeColors[d.outcome] || 'default'}
                      />
                    </TableCell>
                    <TableCell>
                      {d.suggestedInterestRate > 0
                        ? `${d.suggestedInterestRate}%` : 'N/A'}
                    </TableCell>
                    <TableCell>
                      {d.approvedAmount > 0
                        ? `$${d.approvedAmount?.toLocaleString()}` : 'N/A'}
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Box>
  );
}