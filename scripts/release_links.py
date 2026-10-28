"""Resolve repository Markdown links for the flattened research download."""
import posixpath
import re
from pathlib import PurePosixPath
from urllib.parse import quote, unquote, urlsplit

LINK = re.compile(r'''(?P<open>!?\[[^\]\n]*\]\()(?P<target><[^>\n]+>|[^\s)]+)(?P<close>(?:\s+["'][^"'\n]*["'])?\s*\))''')
REPOSITORY = "https://github.com/ipeirotis/Intrade-Archive"


def rewrite_markdown_links(text, source_path, bundled_files, repository_root, revision):
    """Use bundle basenames or commit URLs, validating relative link targets."""
    def replace(match):
        original = match["target"]
        angled = original.startswith("<")
        target = original[1:-1] if angled else original
        parts = urlsplit(target)
        if parts.scheme or parts.netloc or not parts.path:
            return match[0]
        resolved = posixpath.normpath(posixpath.join(
            str(PurePosixPath(source_path).parent), unquote(parts.path)))
        path = PurePosixPath(resolved)
        if path.is_absolute() or resolved == ".." or resolved.startswith("../"):
            raise ValueError(f"Link escapes repository: {source_path}: {target}")
        local = repository_root.joinpath(*path.parts)
        if not local.exists():
            raise ValueError(f"Missing link target: {source_path}: {target}")
        if resolved in bundled_files:
            rewritten = quote(bundled_files[resolved], safe="/")
        else:
            kind = "tree" if local.is_dir() else "blob"
            rewritten = f"{REPOSITORY}/{kind}/{quote(revision, safe='')}/{quote(resolved, safe='/')}"
        if parts.query:
            rewritten += "?" + parts.query
        if parts.fragment:
            rewritten += "#" + parts.fragment
        if angled:
            rewritten = "<" + rewritten + ">"
        return match["open"] + rewritten + match["close"]

    return LINK.sub(replace, text)
