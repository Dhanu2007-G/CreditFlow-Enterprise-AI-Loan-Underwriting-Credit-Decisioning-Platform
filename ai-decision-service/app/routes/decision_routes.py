from fastapi import APIRouter, HTTPException
from app.models.schemas import LoanDecisionRequest, LoanDecisionResponse
from app.services.loan_analyzer import LoanAnalyzer
from app.services.loan_agent import LoanAgent
import logging

logger = logging.getLogger(__name__)
router = APIRouter()

analyzer = LoanAnalyzer()
agent = LoanAgent()

@router.post("/analyze", response_model=LoanDecisionResponse)
async def analyze_loan(request: LoanDecisionRequest):
    """Simple LangChain analysis — single LLM call"""
    logger.info(f"Received analysis request for application: {request.application_id}")
    try:
        return analyzer.analyze(request)
    except Exception as e:
        logger.error(f"Analysis failed: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/analyze/agent", response_model=LoanDecisionResponse)
async def analyze_loan_agent(request: LoanDecisionRequest):
    """LangGraph agentic analysis — multi-step reasoning"""
    logger.info(f"Received agent analysis request for application: {request.application_id}")
    try:
        return agent.analyze(request)
    except Exception as e:
        logger.error(f"Agent analysis failed: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/health")
async def health():
    return {"status": "UP", "service": "ai-decision-service"}