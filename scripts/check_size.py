"""Kotlin 파일 120줄(테스트 200줄) · @Composable 함수 30줄 상한 검사. 사용: python3 check_size.py <루트>"""
import ast
import pathlib
import re
import sys

FILE_MAX, TEST_FILE_MAX, COMPOSABLE_MAX, PY_FUNC_MAX = 120, 200, 30, 15
TEST_DIRS = ('test', 'androidTest', 'androidUnitTest', 'screenshotTest')
FUN = re.compile(r'^\s*(?:(?:private|internal|public|protected|override|suspend|inline|operator|infix)\s+)*fun ')


STRING_LITERAL = re.compile(r'""".*?"""|"(?:\\.|[^"\\])*"')


def braces(line):
    """문자열 속 괄호는 빼고 { 수 - } 수."""
    code = STRING_LITERAL.sub('""', line)
    return code.count('{') - code.count('}')


def is_composable(lines, i):
    """fun 직전 3줄 안에 @Composable이 있으면 화면 함수."""
    return any('@Composable' in lines[k] for k in range(max(0, i - 3), i + 1))


def func_lengths(lines):
    """중괄호 깊이로 fun 블록 길이를 잰다 (한 줄 안 문자열 속 괄호는 뺌)."""
    for i, line in enumerate(lines):
        if not FUN.match(line) or not is_composable(lines, i):
            continue
        depth, started = 0, False
        for j in range(i, len(lines)):
            depth += braces(lines[j])
            started = started or '{' in STRING_LITERAL.sub('""', lines[j])
            if started and depth <= 0:
                yield i + 1, j - i + 1
                break


def py_errors(root):
    """tools/ Python: 파일 120줄 · 함수 15줄 (ast로 정확히 잼)."""
    for path in pathlib.Path(root).glob('tools/**/*.py'):
        source = path.read_text(encoding='utf-8')
        if len(source.splitlines()) > FILE_MAX:
            yield f'{path}: 파일 {len(source.splitlines())}줄 > {FILE_MAX}'
        for node in ast.walk(ast.parse(source)):
            if isinstance(node, (ast.FunctionDef, ast.AsyncFunctionDef)):
                n = node.end_lineno - node.lineno + 1
                if n > PY_FUNC_MAX:
                    yield f'{path}:{node.lineno}: 함수 {n}줄 > {PY_FUNC_MAX}'


def main(root):
    errors = []
    for path in pathlib.Path(root).rglob('*.kt'):
        if 'build' in path.parts:
            continue
        lines = path.read_text(encoding='utf-8').splitlines()
        limit = TEST_FILE_MAX if any(p in TEST_DIRS for p in path.parts) else FILE_MAX
        if len(lines) > limit:
            errors.append(f'{path}: 파일 {len(lines)}줄 > {limit}')
        for start, n in func_lengths(lines):
            if n > COMPOSABLE_MAX:
                errors.append(f'{path}:{start}: 화면 함수 {n}줄 > {COMPOSABLE_MAX}')
    errors += list(py_errors(root))
    print('\n'.join(errors) or 'size OK')
    return 1 if errors else 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1] if len(sys.argv) > 1 else '.'))
