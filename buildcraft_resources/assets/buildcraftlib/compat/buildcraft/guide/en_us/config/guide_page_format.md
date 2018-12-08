Guide pages are all written in a subset of markdown.

<italic>(Most of this page needs filling out, however here is a list of basic tags)</italic>

## Simple single tags

&lt;new_page&gt; is the only single tag: it will move the content after it to the next page. 

## Simple double tags

All of these must surround some content: and they must have both a starting tag &lt;italic&gt; and an ending tag &lt;/italic&gt;
They all affect the contained text between the starting and end tags.

- <italic>italic</italic>
- <bold>bold</bold>
- <underline>underlined</underline>
- <strikethrough>strikethrough</strikethrough>
- <red>red</red>
- <black>black</black>
- <dark_blue>dark_blue</dark_blue>
- <dark_green>dark_green</dark_green>
- <dark_aqua>dark_aqua</dark_aqua>
- <dark_red>dark_red</dark_red>
- <dark_purple>dark_purple</dark_purple>
- <gold>gold</gold>
- <gray>gray</gray>
- <dark_gray>dark_gray</dark_gray>
- <blue>blue</blue>
- <green>green</green> (green)
- <aqua>aqua</aqua> (aqua)
- <light_purple>light_purple</light_purple>
- <yellow>yellow</yellow> (yellow)
- <white>white</white> (white)

## More tags that need to be documented

- lore: Only show this content if lore is enabled
- no_lore: Only show this content if lore is disabled
- recipe: Shows all recipes for the given stack. For example:
<guide_md>
&lt;recipes stack="buildcraftcore:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftcore:gear_wood"/>
- usages: Shows all usages for the given stack. For example:
<guide_md>
&lt;usages stack="buildcraftcore:engine"&gt;
</guide_md>
<usages stack="buildcraftcore:engine"/>
- recipes_usages: show both recipes and usages for the given item. For example:
<guide_md>
&lt;recipes_usages stack="buildcrafttransport:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcrafttransport:pipe_structure"/>
- group: Show related groups.
<new_page/>
- Image: Show an image. For example:
<guide_md>
&lt;image src="buildcraftcore:items/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftcore:items/wrench" width="64" height="64"/>

