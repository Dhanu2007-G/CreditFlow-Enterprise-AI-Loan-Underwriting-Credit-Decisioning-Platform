from langchain_openai import ChatOpenAI
from langchain.schema import HumanMessage, SystemMessage
from app.models.schemas import LoanDecisionRequest, LoanDecisionResponse
from app.config.settings import settings
import json
import logging

logger = logging.getLogger(__name__)

class LoanAnalyzer:

    def __init__(self):
        self.llm = ChatOpenAI(
            model=settings.openai_model,
            api_key=settings.openai_api_key,
            temperature=0.1
        )

    def analyze(self, request: LoanDecisionRequest) -> LoanDecisionResponse:
        logger.info(f"Analyzing loan application: {request.application_id}")

        system_prompt = """You are an expert loan underwriter at a financial institution.
        Your role is to analyze loan applications and provide detailed credit decisions.
        You must respond ONLY with a valid JSON object, no markdown, no code blocks.

        Analyze the application and return this exact JSON structure:
        {
            "decision": "APPROVE or REJECT or CONDITIONAL_APPROVAL",
            "explanation": "detailed explanation of your decision",
            "recommended_action": "specific action to take",
            "confidence": 0.0 to 1.0,
            "suggested_interest_rate": numeric value like 5.99,
            "approved_amount": numeric value or 0 if rejected,
            "reasoning_steps": ["step 1", "step 2", "step 3"]
        }"""

        user_prompt = f"""Analyze this loan application:

        Application ID: {request.application_id}
        Applicant: {request.applicant_name}
        Loan Amount: ${request.loan_amount:,.2f}
        Loan Purpose: {request.loan_purpose}
        Loan Term: {request.loan_term_months} months
        Annual Income: ${request.annual_income:,.2f}
        Credit Score: {request.credit_score}
        Monthly Debt: ${request.monthly_debt:,.2f}
        Employment Type: {request.employment_type}
        Employment Years: {request.employment_years}
        Risk Score: {request.risk_score}/100
        Risk Level: {request.risk_level}
        Risk Factors: {', '.join(request.risk_reasons) if request.risk_reasons else 'None'}

        Provide a comprehensive loan decision with interest rate recommendation."""

        try:
            response = self.llm.invoke([
                SystemMessage(content=system_prompt),
                HumanMessage(content=user_prompt)
            ])

            content = response.content.strip()
            if content.startswith("```"):
                content = content.split("```")[1]
                if content.startswith("json"):
                    content = content[4:]
            content = content.strip()

            if not content:
                raise ValueError("Empty response from LLM")

            result = json.loads(content)
            logger.info(f"Analysis complete for application: {request.application_id} "
                        f"| decision: {result.get('decision')}")

            return LoanDecisionResponse(
                application_id=request.application_id,
                risk_level=request.risk_level,
                risk_score=request.risk_score,
                decision=result.get("decision", "CONDITIONAL_APPROVAL"),
                explanation=result.get("explanation", ""),
                recommended_action=result.get("recommended_action", ""),
                confidence=result.get("confidence", 0.5),
                suggested_interest_rate=result.get("suggested_interest_rate"),
                approved_amount=result.get("approved_amount"),
                reasoning_steps=result.get("reasoning_steps", [])
            )

        except Exception as e:
            logger.error(f"Error analyzing application {request.application_id}: {str(e)}")
            return LoanDecisionResponse(
                application_id=request.application_id,
                risk_level=request.risk_level,
                risk_score=request.risk_score,
                decision="CONDITIONAL_APPROVAL",
                explanation=f"Analysis failed: {str(e)}",
                recommended_action="Manual review required",
                confidence=0.0,
                reasoning_steps=["Analysis failed — manual review required"]
            )