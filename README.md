CFG :

Program -> Identifier Block

Block -> { ( (Declaration | Statement) ; )* }

Declaration -> Type Identifier ( = Expression |  ) | Type IDENTIFIERLIST

IDENTIFIERLIST -> Identifier (, Identifier)*

Type -> int | float | boolean | char | string

Statement -> IfStatement | AssignmentStatement | SleepStatement 
| PrintStatement | WhileStatment

IfStatement -> if ( Expression ) Block

WhileStatement -> while ( Expression ) Block

AssignmentStatement -> Identifier = Expression

SleepStatement -> sleep Expression

PrintStatement -> print Expression

Expression -> OrExpression ? Expression : Expression | OrExpression

OrExpression -> AndExpression ( | AndExpression )*

AndExpression -> EqExpression ( & EqExpression )*

EqExpression -> RelExpression ( ( == | != ) RelExpression )*

RelExpression -> AddExpression ( ( < | > | <= | >= ) AddExpression )*

AddExpression -> MultExpression ( ( + | - ) MultExpression )*

MultExpression -> PowerExpression ( ( * | / | % ) PowerExpression )*

PowerExpression -> UnaryExpression ( ** PowerExpression |   )

UnaryExpression -> + UnaryExpression | - UnaryExpression | ! UnaryExpression | Primary

Primary -> INTEGER_LITERAL | BOOLEAN_LITERAL ​| ​FLOAT_LITERAL | CHAR_LITERAL
 ​| ​STRING_LITERAL | ( Expression ) | IDENTIFIER | Function

Function -> FunctionName ( Expression )

FunctionName -> sin | cos | atan | abs | log | int | float


