from pydantic import BaseModel
from typing import List, Optional
from enum import Enum

class RiskLevel(str, Enum):
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"
    CRITICAL = "CRITICAL"

class LoanDecisionRequest(BaseModel):
    application_id: str
    applicant_name: str
    loan_amount: float
    loan_purpose: str
    loan_term_months: int
    annual_income: float
    credit_score: int
    monthly_debt: float
    employment_type: str
    employment_years: int
    risk_score: float
    risk_level: RiskLevel
    risk_reasons: List[str]

class LoanDecisionResponse(BaseModel):
    application_id: str
    risk_level: RiskLevel
    risk_score: float
    decision: str
    explanation: str
    recommended_action: str
    confidence: float
    suggested_interest_rate: Optional[float] = None
    approved_amount: Optional[float] = None
    reasoning_steps: List[str]