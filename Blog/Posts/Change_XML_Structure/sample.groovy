@Grapes(
    @Grab(group='xmlunit', module='xmlunit', version='1.3')
)
import org.custommonkey.xmlunit.*
import groovy.xml.*

def xml = '''
&lt;todo&gt;
    &lt;item priority="2"&gt;
        &lt;title&gt;Look into GPars&lt;/title&gt;
    &lt;/item&gt;
    &lt;item priority="1"&gt;
        &lt;title&gt;Start developing Griffon App&lt;/title&gt;
    &lt;/item&gt;
    &lt;item priority="2"&gt;
        &lt;title&gt;Grails 1.4 M1&lt;/title&gt;
    &lt;/item&gt;
    &lt;item priority="3"&gt;
        &lt;title&gt;GWT Sample&lt;/title&gt;
    &lt;/item&gt;
&lt;/todo&gt;
'''

def todo = new XmlSlurper().parseText(xml)

// Change node values.
def items = todo.item.findAll { 
   it.@priority.toInteger() &lt; 3 
}
items.each { item -&gt; 
    item.title = "DO: " + item.title 
}

// Change attribute value.
def gpars= todo.item.find { 
    it.title =~ /.*GPars.*/ 
}
gpars.@priority = '1'

// Add extra item node.
todo.appendNode {
    item(priority: 2) {
        title 'Work on blog post'
    }
}

// Change node.
def grailsItem = todo.item.find { 
    it.title.toString().contains('Grails') 
}
grailsItem.replaceNode { node -&gt;
    item(who: 'mrhaki', priority: node.@priority) {
        title 'Download Grails 1.4 M1'       
    }
}

// Remove node. Index value based on result directly after parsing.
// So here we remove the item about GWT.
todo.item[3].replaceNode {}

// Create output.
def newTodo = new StreamingMarkupBuilder().bind { 
    mkp.yield todo 
}.toString()

def expected = '''
&lt;todo&gt;
    &lt;item priority="1"&gt;
        &lt;title&gt;DO: Look into GPars&lt;/title&gt;
    &lt;/item&gt;
    &lt;item priority="1"&gt;
        &lt;title&gt;DO: Start developing Griffon App&lt;/title&gt;
    &lt;/item&gt;
    &lt;item who="mrhaki" priority="2"&gt;
        &lt;title&gt;Download Grails 1.4 M1&lt;/title&gt;
    &lt;/item&gt;
    &lt;item priority="2"&gt;
        &lt;title&gt;Work on blog post&lt;/title&gt;
    &lt;/item&gt;
&lt;/todo&gt;
'''

// Check to see expected XML equals new todo XML.
XMLUnit.ignoreWhitespace = true
def difference = new Diff(newTodo, expected)
assert difference.similar()
