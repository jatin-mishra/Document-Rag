from pathlib import Path

from jinja2 import Environment, FileSystemLoader, StrictUndefined

_TEMPLATE_DIR = Path(__file__).parent / "templates"

_env = Environment(
    loader=FileSystemLoader(str(_TEMPLATE_DIR)),
    undefined=StrictUndefined,
    trim_blocks=True,
    lstrip_blocks=True,
    autoescape=False,
)


def render(template_name: str, **ctx: object) -> str:
    return _env.get_template(template_name).render(**ctx)


def render_rag_prompt(query: str, context: list[str]) -> str:
    return render("rag_user.j2", query=query, context=context)
