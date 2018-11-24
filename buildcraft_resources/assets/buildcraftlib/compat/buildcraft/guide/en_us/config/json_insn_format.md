# How to write Insn files
Files should be located in [...]
On the very first line should be
<json_insn>
~{buildcraft/json/insn}
</json_insn>
Everything else must be a function call or a comment. There are 6 different builtin functions: debug, add, remove, replace, overwrite, modify, and alias.

<new_page/>
## Function arguments
Functions can take 3 different types of arguments: either numbers, strings, or multi-line strings.
Numbers cannot contain spaces.
Strings must be started and finished with a quote (")
Multi-line strings must be started and finished with backticks (`)
For example with the function "debug":
<json_insn>
debug 42.7 // Will error because debug can't take a number
debug "on"
debug `
all
`
</json_insn>

<new_page/>
## The debug function
This function takes one argument: either "all" or "on".
In other words it must be written exactly as either of these:
<json_insn>
debug "on"

debug "all"
</json_insn>

<new_page/>
## The remove function
This function will remove the entry with the given name. This takes 1 argument, the name.
For example:
<json_insn>
remove "buildcraftsilicon:diamond_chipset"
</json_insn>
It is probably easiest to find out the name of entries by turning on debugging with debug "all".

<new_page/>
## The add function
This function will add an additional entry with the given name. This either takes 1 argument (the name), or 2 arguments (the name and then the json).
For example:
<json_insn>
add "item/wrench" `{
    "title": "item.wrenchItem.name",
    "tag_type": "item",
    "tag_subtype": "tool"
}`
</json_insn>
If the json is missing then it will be loaded from the file specified by the name.
For example if the function call is:
<json_insn>
add "wrench"
</json_insn>
And the insn file is in 
<json_insn>
"assets/buildcraftcore/compat/buildcraft/guide.txt"
</json_insn>
then the wrench guide page will be loaded from
<json_insn>
"assets/buildcraftcore/compat/buildcraft/guide/item/wrench.md"
</json_insn>

<new_page/>
## Custom functions with alias
