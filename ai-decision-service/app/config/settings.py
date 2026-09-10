import os
from dotenv import load_dotenv

load_dotenv()

class Settings:
    openai_api_key: str = os.getenv("OPENAI_API_KEY", "")
    openai_model: str = "gpt-4o"
    application_service_url: str = os.getenv(
        "APPLICATION_SERVICE_URL", "http://localhost:8081"
    )
    underwriting_service_url: str = os.getenv(
        "UNDERWRITING_SERVICE_URL", "http://localhost:8082"
    )

settings = Settings()