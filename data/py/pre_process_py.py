import json as json
import ast
from tqdm import tqdm


unicode = lambda s: s


def clean_code(code):
    return code.replace(
        ' DCNL DCSP ', '\n\t'
    ).replace(
        ' DCNL  DCSP ', '\n\t'
    ).replace(
        ' DCNL   DCSP ', '\n\t'
    ).replace(
        ' DCNL ', '\n'
    ).replace(' DCSP ', '\t')


def parse_file(code_str):
    tree = ast.parse(code_str)

    json_tree = []

    def gen_identifier(identifier, node_type='identifier'):
        pos = len(json_tree)
        json_node = {}
        json_tree.append(json_node)
        json_node['type'] = node_type
        json_node['value'] = identifier
        return pos

    def traverse_list(l, node_type='list'):
        pos = len(json_tree)
        json_node = {}
        json_tree.append(json_node)
        json_node['type'] = node_type
        children = []
        for item in l:
            children.append(traverse(item))
        if len(children) != 0:
            json_node['children'] = children
        return pos

    def traverse(node):
        pos = len(json_tree)
        json_node = {}
        json_tree.append(json_node)
        json_node['type'] = type(node).__name__
        children = []
        if isinstance(node, ast.Name):
            json_node['value'] = node.id
        elif isinstance(node, ast.Num):
            json_node['value'] = str(unicode(node.n))
        elif isinstance(node, ast.Str):
            json_node['value'] = "string"
        elif isinstance(node, ast.alias):
            json_node['value'] = unicode(node.name)
            if node.asname:
                children.append(gen_identifier(node.asname))
        elif isinstance(node, ast.FunctionDef):
            json_node['value'] = unicode(node.name)
        elif isinstance(node, ast.ClassDef):
            json_node['value'] = unicode(node.name)
        elif isinstance(node, ast.ImportFrom):
            if node.module:
                json_node['value'] = unicode(node.module)
        elif isinstance(node, ast.Global):
            for n in node.names:
                children.append(gen_identifier(n))
        elif isinstance(node, ast.keyword):
            json_node['value'] = unicode(node.arg)

        # Process children.
        if isinstance(node, ast.For):
            children.append(traverse(node.target))
            children.append(traverse(node.iter))
            children.append(traverse_list(node.body, 'body'))
            if node.orelse:
                children.append(traverse_list(node.orelse, 'orelse'))
        elif isinstance(node, ast.If) or isinstance(node, ast.While):
            children.append(traverse(node.test))
            children.append(traverse_list(node.body, 'body'))
            if node.orelse:
                children.append(traverse_list(node.orelse, 'orelse'))
        elif isinstance(node, ast.With):
            children.append(traverse(node.context_expr))
            if node.optional_vars:
                children.append(traverse(node.optional_vars))
            children.append(traverse_list(node.body, 'body'))
        elif isinstance(node, ast.TryExcept):
            children.append(traverse_list(node.body, 'body'))
            children.append(traverse_list(node.handlers, 'handlers'))
            if node.orelse:
                children.append(traverse_list(node.orelse, 'orelse'))
        elif isinstance(node, ast.TryFinally):
            children.append(traverse_list(node.body, 'body'))
            children.append(traverse_list(node.finalbody, 'finalbody'))
        elif isinstance(node, ast.arguments):
            children.append(traverse_list(node.args, 'args'))
            children.append(traverse_list(node.defaults, 'defaults'))
            if node.vararg:
                children.append(gen_identifier(node.vararg, 'vararg'))
            if node.kwarg:
                children.append(gen_identifier(node.kwarg, 'kwarg'))
        elif isinstance(node, ast.ExceptHandler):
            if node.type:
                children.append(traverse_list([node.type], 'type'))
            if node.name:
                children.append(traverse_list([node.name], 'name'))
            children.append(traverse_list(node.body, 'body'))
        elif isinstance(node, ast.ClassDef):
            children.append(traverse_list(node.bases, 'bases'))
            children.append(traverse_list(node.body, 'body'))
            children.append(traverse_list(node.decorator_list, 'decorator_list'))
        elif isinstance(node, ast.FunctionDef):
            children.append(traverse(node.args))
            children.append(traverse_list(node.body, 'body'))
            children.append(traverse_list(node.decorator_list, 'decorator_list'))
        else:
            # Default handling: iterate over children.
            for child in ast.iter_child_nodes(node):
                if isinstance(child, ast.expr_context) \
                        or isinstance(child, ast.operator) \
                        or isinstance(child, ast.boolop) \
                        or isinstance(child, ast.unaryop) or isinstance(child, ast.cmpop):
                    # Directly include expr_context, and operators into the type instead of creating a child.
                    json_node['type'] = json_node['type'] + type(child).__name__
                else:
                    children.append(traverse(child))

        if isinstance(node, ast.Attribute):
            children.append(gen_identifier(node.attr, 'attr'))

        if len(children) != 0:
            json_node['children'] = children
        return pos

    traverse(tree)
    return json.dumps(json_tree, separators=(',', ':'), ensure_ascii=False)


def read_file(file_name):
    with open(file_name, 'r') as f:
        return f.readlines()


def save_file(data, file_name):
    with open(file_name, 'w') as f:
        for d in data:
            if not d.endswith('\n'):
                d = d + '\n'
            f.write(d)


def generate_pairs(origin_code_file, code_token_file, nl_file):
    origin_code_list = read_file(origin_code_file)
    code_token_list = read_file(code_token_file)
    nl_list = read_file(nl_file)

    ast_list = []
    code_tokens = []
    nls = []

    for i, origin_code in tqdm(enumerate(origin_code_list), 'generate ast pairs..'):
        origin_code = clean_code(origin_code)
        try:
            code_ast = parse_file(origin_code)

            ast_list.append(code_ast)
            code_tokens.append(code_token_list[i])
            nls.append(nl_list[i])
        except SyntaxError:
            print('error...')
            continue

    return ast_list, code_tokens, nls


if __name__ == '__main__':
    work_dir = '/Users/tangze/study/test/'
    data_sets = ['test', 'dev', 'train']
    for data_set in data_sets:
        code_file = work_dir + data_set + '/code.original'
        token_file = work_dir + data_set + '/code.original_subtoken'
        doc_file = work_dir + data_set + '/javadoc.original'

        ast_list, code_tokens, nls = generate_pairs(code_file, token_file, doc_file)

        save_file(ast_list, work_dir + data_set + '/ast.original')
        save_file(code_tokens, work_dir + data_set + '/code.token')
        save_file(nls, work_dir + data_set + '/nl.original')


