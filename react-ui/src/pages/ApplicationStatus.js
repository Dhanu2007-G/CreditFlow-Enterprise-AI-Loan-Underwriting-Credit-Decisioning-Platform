import React, { useState } from 'react';
import {
  Box, Card, CardContent, Typography, TextField, Button,
  Alert, CircularProgress, Chip, Divider, Grid, Paper
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { getLoanById, getDecisionByApplicationId } from '../services/api';

const statusColors = {
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'error',
  CONDITIONAL_APPROVAL: 'info',
  UNDER_REVIEW: 'warning'
};

const outcomeColors = {
  APPROVED: 'success',
  REJECTED: 'error',
  CONDITIONAL_APPROVAL: 'info',
  UNDER_REVIEW: 'warning'
};

export default function ApplicationStatus() {
  const [applicationId, setApplicationId] = useState('');
  const [loading, setLoading] = useState(false);
  const [application, setApplication] = useState(null);
  const [decision, setDecision] = useState(null);
  const [error, setError] = useState(null);

  const handleSearch = async () => {
    if (!applicationId.trim()) return;
    setLoading(true);
    setError(null);
    setApplication(null);
    setDecision(null);

    try {
      const [appRes, decisionRes] = await Promise.allSettled([
        getLoanById(applicationId),
        getDecisionByApplicationId(applicationId)
      ]);

      if (appRes.status === 'fulfilled') {
        setApplication(appRes.value.data);
      } else {
        setError('Application not found');
        setLoading(false);
        return;
      }

      if (decisionRes.status === 'fulfilled') {
        setDecision(decisionRes.value.data);
      }
    } catch (err) {
      setError('Application not found. Please check the ID and try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Typography variant="h4" fontWeight={700} gutterBottom color="primary">
        Application Status
      </Typography>
      <Typography variant="body1" color="text.secondary" mb={3}>
        Enter your application ID to check the status and underwriting decision.
      </Typography>

      <Card elevation={2} sx={{ mb: 3 }}>
        <CardContent sx={{ p: 3 }}>
          <Box display="flex" gap={2}>
            <TextField
              fullWidth
              label="Application ID"
              value={applicationId}
              onChange={(e) => setApplicationId(e.target.value)}
              placeholder="e.g. 4467de6f-73cf-4650-8be8-1e30e36ec6d2"
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
            />
            <Button
              variant="contained"
              onClick={handleSearch}
              disabled={loading}
              startIcon={loading ? <CircularProgress size={18} /> : <SearchIcon />}
              sx={{ px: 4, backgroundColor: '#1a237e', whiteSpace: 'nowrap' }}
            >
              Search
            </Button>
          </Box>
        </CardContent>
      </Card>

      {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

      {application && (
        <Grid container spacing={3}>
          {/* Application Details */}
          <Grid item xs={12} md={6}>
            <Card elevation={2}>
              <CardContent sx={{ p: 3 }}>
                <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                  <Typography variant="h6" fontWeight={600}>
                    Application Details
                  </Typography>
                  <Chip
                    label={application.status}
                    color={statusColors[application.status] || 'default'}
                    fontWeight={600}
                  />
                </Box>
                <Divider sx={{ mb: 2 }} />
                {[
                  ['Applicant', application.applicantName],
                  ['Email', application.email],
                  ['Loan Amount', `$${application.loanAmount?.toLocaleString()}`],
                  ['Loan Purpose', application.loanPurpose],
                  ['Loan Term', `${application.loanTermMonths} months`],
                  ['Annual Income', `$${application.annualIncome?.toLocaleString()}`],
                  ['Credit Score', application.creditScore],
                  ['Monthly Debt', `$${application.monthlyDebt?.toLocaleString()}`],
                  ['Employment', application.employmentType],
                  ['Applied', new Date(application.createdAt).toLocaleDateString()]
                ].map(([label, value]) => (
                  <Box key={label} display="flex" justifyContent="space-between" mb={1}>
                    <Typography variant="body2" color="text.secondary">{label}</Typography>
                    <Typography variant="body2" fontWeight={500}>{value}</Typography>
                  </Box>
                ))}
                {application.decisionReason && (
                  <Alert severity="info" sx={{ mt: 2 }}>
                    <Typography variant="body2">{application.decisionReason}</Typography>
                  </Alert>
                )}
              </CardContent>
            </Card>
          </Grid>

          {/* Underwriting Decision */}
          <Grid item xs={12} md={6}>
            {decision ? (
              <Card elevation={2}>
                <CardContent sx={{ p: 3 }}>
                  <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                    <Typography variant="h6" fontWeight={600}>
                      Underwriting Decision
                    </Typography>
                    <Chip
                      label={decision.outcome}
                      color={outcomeColors[decision.outcome] || 'default'}
                    />
                  </Box>
                  <Divider sx={{ mb: 2 }} />
                  {[
                    ['Risk Score', `${decision.riskScore}/100`],
                    ['Risk Level', decision.riskLevel],
                    ['DTI Ratio', `${(decision.debtToIncomeRatio * 100).toFixed(1)}%`],
                    ['Approved Amount', decision.approvedAmount > 0
                      ? `$${decision.approvedAmount?.toLocaleString()}` : 'N/A'],
                    ['Interest Rate', decision.suggestedInterestRate > 0
                      ? `${decision.suggestedInterestRate}%` : 'N/A']
                  ].map(([label, value]) => (
                    <Box key={label} display="flex" justifyContent="space-between" mb={1}>
                      <Typography variant="body2" color="text.secondary">{label}</Typography>
                      <Typography variant="body2" fontWeight={500}>{value}</Typography>
                    </Box>
                  ))}

                  {decision.decisionReasons && (
                    <Paper variant="outlined" sx={{ p: 2, mt: 2, bgcolor: '#f5f5f5' }}>
                      <Typography variant="body2" fontWeight={600} mb={1}>
                        Decision Reasons:
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {decision.decisionReasons}
                      </Typography>
                    </Paper>
                  )}
                </CardContent>
              </Card>
            ) : (
              <Card elevation={2}>
                <CardContent sx={{ p: 3, textAlign: 'center' }}>
                  <Typography color="text.secondary">
                    Underwriting decision pending...
                  </Typography>
                </CardContent>
              </Card>
            )}
          </Grid>
        </Grid>
      )}
    </Box>
  );
}