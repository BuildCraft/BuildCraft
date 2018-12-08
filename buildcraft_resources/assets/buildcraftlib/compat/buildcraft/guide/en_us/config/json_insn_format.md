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
For example with the function "remove":
<json_insn>
remove "buildcraftenergy:oil"
remove `
buildcraftenergy:oil
`
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
## The replace function
This function will remove an entry with the first name, and add an entry with the second name but only if the first entry was actually added by something else.
This function takes two arguments: the name to remove, and the name to add.
This also takes an optional third argument, the json to add. (This acts in exactly the same way as the add function, if you ignore the first argument).
<new_page/>
## The modify function
This function acts in a very similar way to the replace function, except that this will inherit the json tags from the removed entry.

For example if we wanted to make a combustion engine fuel recipe using buildcraft oil, but only generate reside if buildcraft factory is installed we might do it like this:
<json_insn>
add "oil" `{
 "fuel": {
  "id": "buildcraftenergy:oil", "amount": 4
 },
 "power": 3
}`
</json_insn>
Then an entry in buildcraftfactory could look like this:
<json_insn>
modify "buildcraftenergy:oil" "oil" `{
 "residue": {
  "id": "buildcraftenergy:residue",
  "amount": 2
 }
}`
</json_insn>
Which would result in only a single fuel recipe being added: "buildcraftfactory:oil"
<json_insn>
{
 "fuel": {
  "id": "buildcraftenergy:oil", "amount": 4
 },
 "residue": {
  "id": "buildcraftenergy:residue",
  "amount": 2
 },
 "power": 3
}
</json_insn>

(Note: combustion engine fuel recipes aren't configurable this way quite yet - you'll have to wait for the MJ update before this is actually valid)
<new_page/>
## Custom functions with alias
(TODO: Explanation)
<json_insn>
alias "add_fuel" 2 `
 add "%0" \`{
  "fuel": {
    "id": "${domain}:%0"
  }
 }\`
`
</json_insn>

## Using pre-made alias files
(TODO: Explanation)
<json_insn>
import "buildcraftlib:util"
</json_insn>

Imported files must start with
<json_insn>
~{buildcraft/json/lib}
</json_insn>
and may optionally include an argument count
<json_insn>
~args 3
</json_insn>