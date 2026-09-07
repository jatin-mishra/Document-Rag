import httpx
from typing import Annotated, AsyncIterator, TypeAlias

from app.clients.embedding.embedding_client import EmbeddingClient
from app.clients.indexing.indexing_client import IndexingClient
from app.services.llm.anthropic import AnthropicProvider
from app.services.llm.llm_proxy import LLMProvider, LLMProxy
from app.services.query_service import QueryService
from .settings import Settings
from functools import lru_cache
from app.services.s3_service import S3Service
from fastapi import Depends, HTTPException, Request
from app.dao.document_dao import DocumentDao
from app.services.document_service import DocumentService
from sqlalchemy.ext.asyncio import AsyncEngine, AsyncSession, create_async_engine, async_sessionmaker


@lru_cache
def get_settings() -> Settings:
    return Settings()

SettingsDep: TypeAlias = Annotated[Settings, Depends(get_settings)]

# use as: orderClient = Annotated[httpx.AsyncClient, Depends(service_client("orders"))]
def service_client(name: str): 
    def _dep(request: Request) -> httpx.AsyncClient:
        client = request.app.state.clients.get(name)
        if client is None:
            raise HTTPException(500, f"HTTP client for '{name}' not configured")
        return client 
    return _dep


def get_indexing_client(client: Annotated[httpx.AsyncClient, Depends(service_client("indexing"))]) -> IndexingClient:
    return IndexingClient(client)

IndexingClientDep: TypeAlias = Annotated[IndexingClient, Depends(get_indexing_client)]

def embedding_client(client: Annotated[httpx.AsyncClient, Depends(service_client("embedding"))]) -> EmbeddingClient:
    return EmbeddingClient(client)

EmbeddingClientDep: TypeAlias = Annotated[httpx.AsyncClient, Depends(embedding_client)]

def get_s3_service(settings: SettingsDep) -> S3Service:
    return S3Service(settings)

S3Dep: TypeAlias = Annotated[S3Service, Depends(get_s3_service)]

def get_async_engine(settings: Settings) -> AsyncEngine:
    return create_async_engine(
        settings.postgres.dsn,
        pool_size=settings.postgres.pool_size,
        max_overflow=settings.postgres.max_overflow,
        pool_recycle=settings.postgres.pool_recycle,
        echo=settings.postgres.echo,
    )

def get_session_maker(engine: AsyncEngine):
    return async_sessionmaker(engine, expire_on_commit=False)

async def get_db_session(request: Request) -> AsyncIterator[AsyncSession]:
    factory = request.app.state.db_sessionmaker 
    async with factory() as session:
        try:
            yield session 
            await session.commit()
        except Exception:
            await session.rollback()
            raise 

DbSession: TypeAlias = Annotated[AsyncSession, Depends(get_db_session)]

def get_document_dao(session: DbSession) -> DocumentDao:
    return DocumentDao(session)

DocumentDaoDep: TypeAlias = Annotated[DocumentDao, Depends(get_document_dao)]

def get_document_service(dao: DocumentDaoDep, s3: S3Dep, indexingClient: IndexingClientDep) -> DocumentService:
    return DocumentService(dao, s3, indexingClient)

DocumentServiceDep: TypeAlias = Annotated[DocumentService, Depends(get_document_service)]

def anthropic_provider(setting: SettingsDep) -> LLMProvider:
    return AnthropicProvider(setting.llm_configuration.anthropic)

AnthropicProviderDep: TypeAlias = Annotated[LLMProvider, Depends(anthropic_provider)]

def llm_proxy(anthropic_provider: AnthropicProviderDep) -> LLMProxy:
    return LLMProxy(anthropic_provider)

LLMProxyDep: TypeAlias = Annotated[LLMProxy, Depends(llm_proxy)]

def query_service(llm_proxy: LLMProxyDep, embedding_client: EmbeddingClientDep) -> QueryService:
    return QueryService(llm_proxy, embedding_client)

QueryServiceDep: TypeAlias = Annotated[QueryService, Depends(query_service)]