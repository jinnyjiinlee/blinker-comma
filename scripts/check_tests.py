"""테스트 거짓 통과 막기: 모듈마다 @Test 수 = 실행 수, 건너뜀 0, 단언 없는 테스트 0. 사용: check_tests.py <루트> (gradle check 뒤)"""
import pathlib
import re
import sys
import xml.etree.ElementTree as ET

TEST_ANNOTATION = re.compile(r'^\s*@(Test|ParameterizedTest)\b')
ASSERTION = re.compile(r'\b(assert\w*|fail)\s*[<(]')
EXPRESSION_BODY = re.compile(r'\)\s*(:\s*\w+\s*)?=')


STRING_LITERAL = re.compile(r'""".*?"""|"(?:\\.|[^"\\])*"')


def braces(line):
    """문자열 속 괄호는 빼고 { 수 - } 수."""
    code = STRING_LITERAL.sub('""', line)
    return code.count('{') - code.count('}')


def test_bodies(lines):
    """@Test 아래 fun 블록 본문을 돌려준다 (중괄호 깊이 — 한 줄 안 문자열은 뺌)."""
    for i, line in enumerate(lines):
        if not TEST_ANNOTATION.match(line):
            continue
        depth, started, body = 0, False, []
        if EXPRESSION_BODY.search(lines[i + 1]):
            yield i + 1, None
            continue
        for j in range(i + 1, len(lines)):
            depth += braces(lines[j])
            started = started or '{' in STRING_LITERAL.sub('""', lines[j])
            body.append(lines[j])
            if started and depth <= 0:
                break
        yield i + 1, '\n'.join(body)


def check_module(module):
    errors, declared = [], 0
    for path in (module / 'src' / 'test').rglob('*.kt'):
        lines = path.read_text(encoding='utf-8').splitlines()
        for start, body in test_bodies(lines):
            declared += 1
            if body is None:
                errors.append(f'{path}:{start}: 테스트는 블록 본문 {{ }}으로 — `= 식`은 Unit이 아니면 JUnit이 조용히 건너뜀')
            elif not ASSERTION.search(body):
                errors.append(f'{path}:{start}: 단언 없는 테스트 (assert…/fail 0개)')
    run = skipped = 0
    for report in (module / 'build' / 'test-results').glob('*/TEST-*.xml'):
        suite = ET.parse(report).getroot()
        run += int(suite.get('tests', 0))
        skipped += int(suite.get('skipped', 0))
    has_main = any((module / 'src' / 'main').rglob('*.kt'))
    if has_main and declared == 0:
        errors.append(f'{module}: main 코드가 있는데 테스트 0개')
    if run < declared:
        errors.append(f'{module}: @Test {declared}개 중 {run}개만 실행됨 (발견 안 된 테스트)')
    if skipped:
        errors.append(f'{module}: 건너뛴 테스트 {skipped}개')
    return errors, run - skipped


def main(root):
    errors, summary = [], []
    for module in sorted(p.parent.parent for p in pathlib.Path(root).glob('*/src/main')):
        module_errors, passed = check_module(module)
        errors += module_errors
        summary.append(f'{module.name} {passed}')
    print('\n'.join(errors) or 'tests OK (' + ', '.join(summary) + ')')
    return 1 if errors else 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1] if len(sys.argv) > 1 else '.'))
