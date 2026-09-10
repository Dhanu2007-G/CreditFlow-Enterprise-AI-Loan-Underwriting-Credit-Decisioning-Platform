from langgraph.graph import StateGraph, END
from langchain_openai import ChatOpenAI
from langchain.schema import HumanMessage
from app.models.schemas import LoanDecisionRequest, LoanDecisionResponse
from app.config.settings import settings
from typing import TypedDict, List
import json
import logging

logger = logging.getLogger(__name__)

class AgentState(TypedDict):
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
    risk_level: str
    risk_reasons: List[str]
    needs_deep_review: bool
    analysis_complete: bool
    decision: str
    explanation: str
    recommended_action: str
    confidence: float
    suggested_interest_rate: float
    approved_amount: float
    reasoning_steps: List[str]

class LoanAgent:

    def __init__(self):
        self.llm = ChatOpenAI(
            model=settings.openai_model,
            api_key=settings.openai_api_key,
            temperature=0.1
        )
        self.graph = self._build_graph()

    def _assess_risk_node(self, state: AgentState) -> AgentState:
        logger.info(f"Agent: assessing risk for application {state['application_id']}")
        steps = state.get("reasoning_steps", [])
        steps.append(f"Initial assessment: credit_score={state['credit_score']}, "
                     f"risk_score={state['risk_score']}, level={state['risk_level']}")

        needs_deep_review = (
                state["risk_score"] >= 40 or
                state["risk_level"] in ["HIGH", "CRITICAL"] or
                state["credit_score"] < 620
        )

        if needs_deep_review:
            steps.append("Escalating to deep credit review — elevated risk detected")
        else:
            steps.append("Proceeding with standard approval analysis")

        return {**state, "needs_deep_review": needs_deep_review,
                "reasoning_steps": steps}

    def _deep_review_node(self, state: AgentState) -> AgentState:
        logger.info(f"Agent: deep review for application {state['application_id']}")
        steps = state.get("reasoning_steps", [])

        prompt = f"""You are a senior loan underwriter performing deep credit review.

        Application Details:
        - Loan Amount: ${state['loan_amount']:,.2f}
        - Annual Income: ${state['annual_income']:,.2f}
        - Credit Score: {state['credit_score']}
        - Monthly Debt: ${state['monthly_debt']:,.2f}
        - Employment: {state['employment_type']} for {state['employment_years']} years
        - Risk Score: {state['risk_score']}/100
        - Risk Level: {state['risk_level']}
        - Risk Factors: {', '.join(state['risk_reasons'])}

        This application requires deep review. Respond ONLY with raw JSON:
        {{
            "decision": "APPROVE or REJECT or CONDITIONAL_APPROVAL",
            "explanation": "detailed credit analysis",
            "recommended_action": "specific action",
            "confidence": 0.0 to 1.0,
            "suggested_interest_rate": numeric,
            "approved_amount": numeric or 0,
            "additional_steps": ["step1", "step2"]
        }}"""

        try:
            response = self.llm.invoke([HumanMessage(content=prompt)])
            content = response.content.strip()
            if content.startswith("```"):
                content = content.split("```")[1]
                if content.startswith("json"):
                    content = content[4:]
            content = content.strip()
            result = json.loads(content)

            steps.append(f"Deep review decision: {result.get('decision')}")
            steps.extend(result.get("additional_steps", []))

            return {
                **state,
                "decision": result.get("decision", "CONDITIONAL_APPROVAL"),
                "explanation": result.get("explanation", ""),
                "recommended_action": result.get("recommended_action", ""),
                "confidence": result.get("confidence", 0.7),
                "suggested_interest_rate": result.get("suggested_interest_rate", 9.99),
                "approved_amount": result.get("approved_amount", 0),
                "reasoning_steps": steps,
                "analysis_complete": True
            }
        except Exception as e:
            logger.error(f"Deep review failed: {str(e)}")
            steps.append(f"Deep review error: {str(e)}")
            return {**state,
                    "decision": "CONDITIONAL_APPROVAL",
                    "explanation": "Deep review failed — manual review required",
                    "recommended_action": "Assign to senior underwriter",
                    "confidence": 0.0,
                    "suggested_interest_rate": 0.0,
                    "approved_amount": 0.0,
                    "reasoning_steps": steps,
                    "analysis_complete": True}

    def _standard_approval_node(self, state: AgentState) -> AgentState:
        logger.info(f"Agent: standard approval for application {state['application_id']}")
        steps = state.get("reasoning_steps", [])

        prompt = f"""Analyze this low-risk loan application for approval:

        - Loan Amount: ${state['loan_amount']:,.2f}
        - Annual Income: ${state['annual_income']:,.2f}
        - Credit Score: {state['credit_score']}
        - Monthly Debt: ${state['monthly_debt']:,.2f}
        - Employment: {state['employment_type']} for {state['employment_years']} years
        - Risk Score: {state['risk_score']}/100
        - Risk Factors: {', '.join(state['risk_reasons']) if state['risk_reasons'] else 'None'}

        Respond ONLY with raw JSON:
        {{
            "decision": "APPROVE or CONDITIONAL_APPROVAL",
            "explanation": "approval analysis",
            "recommended_action": "action to take",
            "confidence": 0.0 to 1.0,
            "suggested_interest_rate": numeric,
            "approved_amount": numeric
        }}"""

        try:
            response = self.llm.invoke([HumanMessage(content=prompt)])
            content = response.content.strip()
            if content.startswith("```"):
                content = content.split("```")[1]
                if content.startswith("json"):
                    content = content[4:]
            content = content.strip()
            result = json.loads(content)
            steps.append(f"Standard approval decision: {result.get('decision')}")

            return {
                **state,
                "decision": result.get("decision", "APPROVE"),
                "explanation": result.get("explanation", ""),
                "recommended_action": result.get("recommended_action", ""),
                "confidence": result.get("confidence", 0.9),
                "suggested_interest_rate": result.get("suggested_interest_rate", 5.99),
                "approved_amount": result.get("approved_amount",
                                              state["loan_amount"]),
                "reasoning_steps": steps,
                "analysis_complete": True
            }
        except Exception as e:
            steps.append(f"Standard approval error: {str(e)}")
            return {**state,
                    "decision": "APPROVE",
                    "explanation": "Low risk application approved",
                    "recommended_action": "Process loan at standard rate",
                    "confidence": 0.8,
                    "suggested_interest_rate": 5.99,
                    "approved_amount": state["loan_amount"],
                    "reasoning_steps": steps,
                    "analysis_complete": True}

    def _should_deep_review(self, state: AgentState) -> str:
        return "deep_review" if state.get("needs_deep_review") else "standard_approval"

    def _build_graph(self) -> StateGraph:
        workflow = StateGraph(AgentState)

        workflow.add_node("assess_risk", self._assess_risk_node)
        workflow.add_node("deep_review", self._deep_review_node)
        workflow.add_node("standard_approval", self._standard_approval_node)

        workflow.set_entry_point("assess_risk")

        workflow.add_conditional_edges(
            "assess_risk",
            self._should_deep_review,
            {
                "deep_review": "deep_review",
                "standard_approval": "standard_approval"
            }
        )

        workflow.add_edge("deep_review", END)
        workflow.add_edge("standard_approval", END)

        return workflow.compile()

    def analyze(self, request: LoanDecisionRequest) -> LoanDecisionResponse:
        logger.info(f"Starting agent analysis for application: {request.application_id}")

        initial_state = AgentState(
            application_id=request.application_id,
            applicant_name=request.applicant_name,
            loan_amount=request.loan_amount,
            loan_purpose=request.loan_purpose,
            loan_term_months=request.loan_term_months,
            annual_income=request.annual_income,
            credit_score=request.credit_score,
            monthly_debt=request.monthly_debt,
            employment_type=request.employment_type,
            employment_years=request.employment_years,
            risk_score=request.risk_score,
            risk_level=request.risk_level,
            risk_reasons=request.risk_reasons,
            needs_deep_review=False,
            analysis_complete=False,
            decision="",
            explanation="",
            recommended_action="",
            confidence=0.0,
            suggested_interest_rate=0.0,
            approved_amount=0.0,
            reasoning_steps=[]
        )

        final_state = self.graph.invoke(initial_state)

        return LoanDecisionResponse(
            application_id=request.application_id,
            risk_level=request.risk_level,
            risk_score=request.risk_score,
            decision=final_state["decision"],
            explanation=final_state["explanation"],
            recommended_action=final_state["recommended_action"],
            confidence=final_state["confidence"],
            suggested_interest_rate=final_state.get("suggested_interest_rate"),
            approved_amount=final_state.get("approved_amount"),
            reasoning_steps=final_state["reasoning_steps"]
        )