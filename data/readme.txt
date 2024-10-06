1. java and py fold list their original datasets
2. processed fold saves our data set
3. ./java/java_ast_parser.zip and ./py/pre_process.py are programs for generating AST from code.
4. In the processed data set, there are 9 files in train, dev and test folds, listed as :

code.seq : file saves code tokens
nl.txt : file saves comments

paths.seq: file saves paths from AST

split_matrices.npz: relationship matrcies of AST (split AST leaf nodes)
split_sbt.seq: SBT sequence of AST  (split AST leaf nodes)
split_pot.seq: POT sequence of AST (split AST leaf nodes)

un_split_matrices.npz : relationship matrcies of AST (without splitting AST leaf nodes)
un_split_sbt.seq: SBT sequence of AST   (without splitting AST leaf nodes)
un_split_pot.seq: POT sequence of AST  (without splitting AST leaf nodes)