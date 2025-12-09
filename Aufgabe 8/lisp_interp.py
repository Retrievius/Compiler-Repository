#!/usr/bin/env python3
"""
Einfacher Tree-walking Interpreter für Lisp-artige Sprache.
Supports:
- atoms: integers, floats, strings "like this", symbols
- lists: (operator arg1 arg2 ...)
Special forms: def, let, defn, do, if
Builtins (native): print, str, list, nth, head, tail
Syntactic sugar: variadic operators (fold left)
Basic semantic checks performed during evaluation.
"""

import sys
import math
import shlex

### Parser: tokenize & parse S-expressions into nested Python lists/atoms

def tokenize(s):
    # We want parentheses as tokens, strings preserved (supports "...")
    # Use shlex to respect quotes, then further split parentheses
    lex = shlex.shlex(s, posix=True)
    lex.whitespace_split = True
    lex.commenters = ';'  # allow ; comments
    tokens = []
    for t in lex:
        # break tokens containing parens e.g. "(+"
        cur = t
        while cur:
            if cur[0] == '(':
                tokens.append('(')
                cur = cur[1:]
            elif cur[0] == ')':
                tokens.append(')')
                cur = cur[1:]
            else:
                # take until next paren
                i = 0
                while i < len(cur) and cur[i] not in '()':
                    i += 1
                tokens.append(cur[:i])
                cur = cur[i:]
    return tokens

def parse_tokens(tokens):
    def parse_expr(i):
        if i >= len(tokens):
            raise SyntaxError("Unexpected EOF while reading")
        tok = tokens[i]
        if tok == '(':
            lst = []
            i += 1
            while i < len(tokens) and tokens[i] != ')':
                expr, i = parse_expr(i)
                lst.append(expr)
            if i >= len(tokens):
                raise SyntaxError("Missing ')'")
            return lst, i+1
        elif tok == ')':
            raise SyntaxError("Unexpected )")
        else:
            atom = atomize(tok)
            return atom, i+1
    exprs = []
    i = 0
    while i < len(tokens):
        expr, i = parse_expr(i)
        exprs.append(expr)
    # if multiple top-level exprs, wrap in (do ...)
    if len(exprs) == 0:
        return None
    if len(exprs) == 1:
        return exprs[0]
    return ['do'] + exprs

def atomize(token):
    # try number
    if token.startswith('"') and token.endswith('"') and len(token) >= 2:
        return token[1:-1]
    # booleans true/false
    if token == 'true':
        return True
    if token == 'false':
        return False
    try:
        if '.' in token:
            return float(token)
        return int(token)
    except:
        return Symbol(token)

### AST helper: Symbol type to differentiate from strings
class Symbol(str):
    pass

def is_symbol(x):
    return isinstance(x, Symbol)

### Syntactic sugar folding
# For variadic arithmetic/comparison operators we fold them left:
VARIADIC_BINOPS = {'+', '-', '*', '/', '<', '>', '<=', '>=', '=='}

def desugar(node):
    """Recursively transform AST: fold variadic operators into left-assoc binary trees."""
    if isinstance(node, list):
        if len(node) == 0:
            return node
        head = node[0]
        # recursively desugar args first
        args = [desugar(x) for x in node[1:]]
        if isinstance(head, Symbol) and head in VARIADIC_BINOPS and len(args) > 2:
            # fold left: (op a b c d) -> (op (op (op a b) c) d)
            acc = [head, args[0], args[1]]
            for a in args[2:]:
                acc = [head, acc, a]
            return acc
        else:
            return [desugar(head)] + args
    else:
        return node

### Environment and functions

class Env:
    def __init__(self, parent=None):
        self.parent = parent
        self.vars = {}

    def define(self, name, value):
        if name in self.vars:
            raise SemanticError(f"Symbol '{name}' already defined in this scope.")
        self.vars[name] = value

    def set(self, name, value):
        env = self.find_env_containing(name)
        if env is None:
            raise SemanticError(f"Symbol '{name}' not defined.")
        env.vars[name] = value

    def find_env_containing(self, name):
        if name in self.vars:
            return self
        if self.parent is not None:
            return self.parent.find_env_containing(name)
        return None

    def get(self, name):
        if name in self.vars:
            return self.vars[name]
        if self.parent is not None:
            return self.parent.get(name)
        raise SemanticError(f"Undefined symbol '{name}'.")

class SemanticError(Exception):
    pass

class LispFunction:
    def __init__(self, params, body, env, name="<lambda>"):
        self.params = params  # list of Symbol names
        self.body = body
        self.env = env
        self.name = name

    def __call__(self, args, evaluator):
        if len(args) != len(self.params):
            raise SemanticError(f"Function '{self.name}' expected {len(self.params)} args, got {len(args)}")
        new_env = Env(parent=self.env)
        for pname, val in zip(self.params, args):
            if not isinstance(pname, Symbol):
                raise SemanticError("Parameter names must be symbols")
            new_env.define(pname, val)
        # evaluate body in new_env; body can be single expr or do-block
        return evaluator.eval(self.body, new_env)

### Evaluator

class Evaluator:
    def __init__(self):
        self.global_env = Env()
        self._install_builtins()

    def _install_builtins(self):
        # native builtins wrapped as callables receiving already-evaluated args
        def builtin_print(args, evaluator):
            # print each arg via Python str()
            out = ' '.join(self._to_string(a) for a in args)
            print(out)
            return None

        def builtin_str(args, evaluator):
            if len(args) != 1:
                raise SemanticError("str expects exactly 1 argument")
            return self._to_string(args[0])

        def builtin_list(args, evaluator):
            return list(args)

        def builtin_nth(args, evaluator):
            if len(args) != 2:
                raise SemanticError("nth expects 2 arguments (list, index)")
            lst, idx = args
            if not isinstance(lst, list):
                raise SemanticError("nth expects a list as first argument")
            if not isinstance(idx, int):
                raise SemanticError("nth expects integer index")
            if idx < 0 or idx >= len(lst):
                raise SemanticError("index out of range")
            return lst[idx]

        def builtin_head(args, evaluator):
            if len(args) != 1:
                raise SemanticError("head expects 1 list")
            lst = args[0]
            if not isinstance(lst, list):
                raise SemanticError("head expects a list")
            if len(lst) == 0:
                return None
            return lst[0]

        def builtin_tail(args, evaluator):
            if len(args) != 1:
                raise SemanticError("tail expects 1 list")
            lst = args[0]
            if not isinstance(lst, list):
                raise SemanticError("tail expects a list")
            return lst[1:]

        # arithmetic / comparison operators as binary (our desugar will ensure binary)
        def numeric_op_factory(fn, name):
            def op(args, evaluator):
                if len(args) != 2:
                    raise SemanticError(f"{name} expects 2 arguments")
                a, b = args
                if not (isinstance(a, (int, float)) and isinstance(b, (int, float))):
                    raise SemanticError(f"{name} expects numeric args")
                return fn(a, b)
            return op

        def cmp_op_factory(fn, name):
            def op(args, evaluator):
                if len(args) != 2:
                    raise SemanticError(f"{name} expects 2 arguments")
                a, b = args
                return fn(a, b)
            return op

        # register builtins
        self.global_env.define(Symbol('print'), ('native', builtin_print))
        self.global_env.define(Symbol('str'), ('native', builtin_str))
        self.global_env.define(Symbol('list'), ('native', builtin_list))
        self.global_env.define(Symbol('nth'), ('native', builtin_nth))
        self.global_env.define(Symbol('head'), ('native', builtin_head))
        self.global_env.define(Symbol('tail'), ('native', builtin_tail))
        self.global_env.define(Symbol('+'), ('native', numeric_op_factory(lambda x,y: x+y, '+')))
        self.global_env.define(Symbol('-'), ('native', numeric_op_factory(lambda x,y: x-y, '-')))
        self.global_env.define(Symbol('*'), ('native', numeric_op_factory(lambda x,y: x*y, '*')))
        self.global_env.define(Symbol('/'), ('native', numeric_op_factory(lambda x,y: x/y if y!=0 else (_raise("Division by zero")), '/')))
        self.global_env.define(Symbol('<'), ('native', cmp_op_factory(lambda a,b: a < b, '<')))
        self.global_env.define(Symbol('>'), ('native', cmp_op_factory(lambda a,b: a > b, '>')))
        self.global_env.define(Symbol('<='), ('native', cmp_op_factory(lambda a,b: a <= b, '<=')))
        self.global_env.define(Symbol('>='), ('native', cmp_op_factory(lambda a,b: a >= b, '>=')))
        self.global_env.define(Symbol('=='), ('native', cmp_op_factory(lambda a,b: a == b, '==')))
        # booleans maybe used as-is

    def _to_string(self, v):
        if v is None:
            return "nil"
        if isinstance(v, bool):
            return "true" if v else "false"
        if isinstance(v, list):
            return "(" + " ".join(self._to_string(x) for x in v) + ")"
        return str(v)

    def eval(self, node, env=None):
        if env is None:
            env = self.global_env
        # node can be Symbol, number, string, bool, list
        # atoms:
        if is_symbol(node):
            # resolve variable
            return env.get(node)
        if isinstance(node, (int, float, str, bool)):
            return node
        if node is None:
            return None
        if isinstance(node, list):
            if len(node) == 0:
                return []
            head = node[0]
            # special forms: def, let, defn, do, if
            if is_symbol(head):
                name = head
                if name == Symbol('def'):
                    # (def name expr) -> define global
                    if len(node) != 3:
                        raise SemanticError("def expects (def name expr)")
                    n = node[1]
                    if not is_symbol(n):
                        raise SemanticError("def: first arg must be a symbol")
                    # check duplicate in this scope
                    if n in env.vars:
                        raise SemanticError(f"Symbol '{n}' already defined in this scope.")
                    val = self.eval(node[2], env)
                    env.define(n, val)
                    return n
                if name == Symbol('let'):
                    # (let ((a 1) (b 2)) body...) or simplified (let (a 1 b 2) body)
                    if len(node) < 3:
                        raise SemanticError("let expects bindings and body")
                    bindings = node[1]
                    body = node[2] if len(node) == 3 else ['do'] + node[2:]
                    new_env = Env(parent=env)
                    # allow two binding styles: list of pairs or flat list
                    if isinstance(bindings, list) and bindings and all(isinstance(x, list) and len(x)==2 for x in bindings):
                        pairs = bindings
                    elif isinstance(bindings, list) and len(bindings) % 2 == 0:
                        pairs = []
                        it = iter(bindings)
                        for a,b in zip(it, it):
                            pairs.append([a,b])
                    else:
                        raise SemanticError("let bindings malformed")
                    for p in pairs:
                        if not is_symbol(p[0]):
                            raise SemanticError("let binding name must be symbol")
                        if p[0] in new_env.vars:
                            raise SemanticError(f"Symbol '{p[0]}' already defined in this let scope.")
                        new_env.define(p[0], self.eval(p[1], new_env))
                    return self.eval(body, new_env)
                if name == Symbol('defn'):
                    # (defn fname (params...) body)
                    if len(node) < 4:
                        raise SemanticError("defn expects (defn name (params...) body...)")
                    fname = node[1]
                    params = node[2]
                    body = ['do'] + node[3:] if len(node) > 4 else node[3]
                    if not is_symbol(fname):
                        raise SemanticError("Function name must be symbol")
                    if not isinstance(params, list) or not all(is_symbol(p) for p in params):
                        raise SemanticError("Function params must be a list of symbols")
                    if fname in env.vars:
                        raise SemanticError(f"Symbol '{fname}' already defined in this scope.")
                    func = LispFunction(params, body, env, name=str(fname))
                    env.define(fname, func)
                    return fname
                if name == Symbol('do'):
                    # evaluate sequence
                    result = None
                    for expr in node[1:]:
                        result = self.eval(expr, env)
                    return result
                if name == Symbol('if'):
                    # (if cond then else?)
                    if len(node) < 3 or len(node) > 4:
                        raise SemanticError("if expects (if cond then [else])")
                    cond = self.eval(node[1], env)
                    if cond:
                        return self.eval(node[2], env)
                    elif len(node) == 4:
                        return self.eval(node[3], env)
                    else:
                        return None
                if name == Symbol('lambda'):
                    # (lambda (params) body) -> returns function
                    if len(node) < 3:
                        raise SemanticError("lambda expects (lambda (params) body)")
                    params = node[1]
                    body = ['do'] + node[2:] if len(node) > 3 else node[2]
                    if not isinstance(params, list) or not all(is_symbol(p) for p in params):
                        raise SemanticError("lambda params must be symbols")
                    return LispFunction(params, body, env, name="<lambda>")
            # otherwise function application: evaluate operator then args
            op_val = self.eval(head, env)
            # evaluate args eagerly
            args = [self.eval(x, env) for x in node[1:]]
            # semantic check: op_val must be callable (native or LispFunction)
            if isinstance(op_val, tuple) and op_val[0] == 'native':
                native_fn = op_val[1]
                return native_fn(args, self)
            elif isinstance(op_val, LispFunction):
                return op_val(args, self)
            else:
                raise SemanticError("Attempt to call a non-function value.")
        # unknown node type
        raise SemanticError(f"Cannot evaluate node: {node}")

def _raise(msg):
    raise SemanticError(msg)

### REPL and file loading

def run_program(source_text, evaluator):
    tokens = tokenize(source_text)
    if not tokens:
        return None
    ast = parse_tokens(tokens)
    ast = desugar(ast)
    return evaluator.eval(ast)

def repl(evaluator):
    print("Lisp-like REPL. Type '(quit)' or Ctrl+C to exit.")
    try:
        while True:
            try:
                line = input('lisp> ')
            except EOFError:
                break
            if not line.strip():
                continue
            if line.strip() == '(quit)':
                break
            try:
                ast = parse_tokens(tokenize(line))
                ast = desugar(ast)
                result = evaluator.eval(ast)
                if result is not None:
                    print("=>", evaluator._to_string(result))
            except Exception as e:
                print("Error:", e)
    except KeyboardInterrupt:
        print("\nExiting REPL.")

### CLI behavior

def main():
    evaluator = Evaluator()
    if len(sys.argv) >= 2:
        # load file
        fname = sys.argv[1]
        with open(fname, 'r', encoding='utf-8') as f:
            src = f.read()
        try:
            run_program(src, evaluator)
            print(f"Loaded {fname}. Entering REPL with preloaded environment.")
        except Exception as e:
            print("Error while loading file:", e)
            return
    repl(evaluator)

if __name__ == '__main__':
    main()
