import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import {
  AppBar, Toolbar, Typography, Button, Box, Container
} from '@mui/material';
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';
import LoanApplicationForm from './pages/LoanApplicationForm';
import ApplicationStatus from './pages/ApplicationStatus';
import AdminDashboard from './pages/AdminDashboard';

function App() {
  return (
    <Router>
      <Box sx={{ flexGrow: 1 }}>
        <AppBar position="static" sx={{ backgroundColor: '#1a237e' }}>
          <Toolbar>
            <AccountBalanceIcon sx={{ mr: 2 }} />
            <Typography variant="h6" sx={{ flexGrow: 1, fontWeight: 700 }}>
              AI Loan Underwriting System
            </Typography>
            <Button color="inherit" component={Link} to="/">
              Apply
            </Button>
            <Button color="inherit" component={Link} to="/status">
              Check Status
            </Button>
            <Button color="inherit" component={Link} to="/admin">
              Admin Dashboard
            </Button>
          </Toolbar>
        </AppBar>

        <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
          <Routes>
            <Route path="/" element={<LoanApplicationForm />} />
            <Route path="/status" element={<ApplicationStatus />} />
            <Route path="/admin" element={<AdminDashboard />} />
          </Routes>
        </Container>
      </Box>
    </Router>
  );
}

export default App;