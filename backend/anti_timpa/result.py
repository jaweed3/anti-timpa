from __future__ import annotations

from dataclasses import dataclass
from typing import Generic, TypeVar

__all__ = ["Result"]

T = TypeVar("T")
E = TypeVar("E")


@dataclass(frozen=True)
class Result(Generic[T, E]):
    value: T | None = None
    error: E | None = None

    @staticmethod
    def ok(value: T) -> Result[T, E]:
        return Result(value=value, error=None)

    @staticmethod
    def err(error: E) -> Result[T, E]:
        return Result(value=None, error=error)

    def is_ok(self) -> bool:
        return self.error is None

    def is_err(self) -> bool:
        return self.error is not None

    def unwrap(self) -> T:
        if self.error is not None:
            raise RuntimeError(f"Called unwrap on error: {self.error}")
        assert self.value is not None
        return self.value

    def unwrap_or(self, default: T) -> T:
        if self.error is not None:
            return default
        assert self.value is not None
        return self.value
