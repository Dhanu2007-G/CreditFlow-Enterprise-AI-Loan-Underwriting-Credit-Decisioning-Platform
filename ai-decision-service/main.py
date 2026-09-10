from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routes import decision_routes
import uvicorn

app = FastAPI(
    title="AI Decision Service",
    description="LangChain + LangGraph powered loan underwriting decision service",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(
    decision_routes.router,
    prefix="/api/v1/ai",
    tags=["AI Decision"]
)

@app.get("/health")
def health():
    return {"status": "UP", "service": "ai-decision-service"}

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8084, reload=True)