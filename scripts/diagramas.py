"""Genera la vista de implementación desde las clases compiladas (no modifica las referencias)."""
from pathlib import Path
import os, re, subprocess
os.chdir(Path(__file__).resolve().parent.parent)
# Permite usar el mismo JDK detectado por los scripts de ejecución.
java_home = os.environ.get('JAVA_HOME')
if not java_home:
    import shutil
    if not shutil.which('javap'):
        candidates = list((Path.home()/'.jdks').glob('*/bin/javap')) + list(Path('/usr/lib/jvm').glob('*/bin/javap'))
        if candidates:
            java_home = str(candidates[0].parent.parent)
javap = str(Path(java_home)/'bin/javap') if java_home else 'javap'
if not Path('target/classes').is_dir():
    raise SystemExit('Primero compilar el proyecto con scripts/run.sh o scripts/test.sh.')
out = ['@startuml', 'skinparam classAttributeIconSize 0', 'hide empty members',
       'title CU28 - Vista de implementación Java (complemento de la página 2)']
relations = []
known = {p.stem for p in Path('src').glob('*/*.java')}
def simple(s):
    return re.sub(r'(?:\b[a-z_]\w*\.)+', '', s)
for source in sorted(Path('src').glob('*/*.java')):
    name = source.stem
    package = source.parent.name
    data = subprocess.check_output([javap, '-p', '-classpath', 'target/classes', package+'.'+name], text=True)
    header = next(l for l in data.splitlines() if l.endswith('{'))
    kind = 'interface' if ' interface ' in header else 'abstract class' if 'abstract class' in header else 'class'
    out.append(f'{kind} {name} {{')
    for line in data.splitlines():
        line = line.strip()
        if not line.endswith(';') or line.startswith('Compiled') or '$' in line or line == 'static {};': continue
        visibility = '+' if line.startswith('public ') else '#' if line.startswith('protected ') else '-'
        body = re.sub(r'^(public|protected|private) ', '', line).rstrip(';')
        static = '{static} ' if body.startswith('static ') else ''
        body = re.sub(r'\b(static|final|abstract|synchronized)\s+', '', body)
        body = simple(body)
        if '(' not in body:
            field_type, field = body.rsplit(' ', 1)
            out.append(f'  {visibility} {static}{field}: {field_type}')
            target = re.sub(r'List<([^>]+)>', r'\1', field_type)
            if target in known: relations.append(f'{name} --> {target} : {field}')
        else:
            out.append(f'  {visibility} {static}{body}')
    out.append('}')
    parent = re.search(r'extends ([\w.]+)', header)
    if parent and simple(parent.group(1)) in known: relations.append(f'{simple(parent.group(1))} <|-- {name}')
    implemented = re.search(r'implements ([\w.]+)', header)
    if implemented and simple(implemented.group(1)) in known: relations.append(f'{simple(implemented.group(1))} <|.. {name}')
out += sorted(set(relations)) + ['@enduml']
Path('docs/diagramas/clases-implementacion.puml').write_text('\n'.join(out)+'\n')
