Zinc - An unofficial fork of CSNePS to turn it into a library
======

# _About_

This is an experiment to turn [CSNePS](https://github.com/SNePS/CSNePS/tree/semtype-objectlang) into a library instead of a standalone application. For starters, I removed the GUI and the -main function. A few other changes were needed, and probably a few more will be. Still, at this point all (or nearly all) of the substantial code is from the original. I'm changing the name to avoid confusion and trademark problems. I have no affiliation with any of the people and institutions involved in creating CSNePS, and no endorsement from them should be assumed. For more information on CSNePS, see the link.

I got some basic functionality working, but there's still a lot to do.




# _Availability_

Zinc is available in [Clojars](https://clojars.org/).

Simply add the following to your project:

[![Clojars Project](http://clojars.org/org.clojars.martinodb/zinc/latest-version.svg)](http://clojars.org/org.clojars.martinodb/zinc)


# _Usage_

Once added to your project, do as in the following example.

```
$ lein repl
Knowledge Base cleared. Contexts, slots, caseframes, and semantic types reinitialized.
nREPL server started on port 38109 on host 127.0.0.1 - nrepl://127.0.0.1:38109
REPL-y 0.4.3, nREPL 0.5.3
Clojure 1.9.0
Java HotSpot(TM) 64-Bit Server VM 10.0.2+13
    Docs: (doc function-name-here)
          (find-doc "part-of-name-here")
  Source: (source function-name-here)
 Javadoc: (javadoc java-object-or-class-here)
    Exit: Control+D or (exit) or (quit)
 Results: Stored in vars *1, *2, *3, an exception in *e

bobtailbot.core=> (require '[csneps.core.snuser :as s])
nil
bobtailbot.core=> (s/clearkb true)
Knowledge Base cleared. Contexts, slots, caseframes, and semantic types reinitialized.
nil
bobtailbot.core=> (s/ask '(Isa Fido Dog))
I wonder if wft17?: (Isa Fido Dog)
I will consider using Slot&Path-Based inference.
I wonder if wft18?: (not (Isa Fido Dog))
I will consider using Slot&Path-Based inference.
#{}
bobtailbot.core=> (s/assert! '(Isa Fido Dog))
wft17!: (Isa Fido Dog)
bobtailbot.core=> (s/ask '(Isa Fido Dog))
I wonder if wft17!: (Isa Fido Dog)
I know that wft17!: (Isa Fido Dog)
#{wft17!: (Isa Fido Dog)}
bobtailbot.core=> (s/unassert '(Isa Fido Dog))
nil
bobtailbot.core=> (s/ask '(Isa Fido Dog))
I wonder if wft17?: (Isa Fido Dog)
I will consider using Slot&Path-Based inference.
I wonder if wft18?: (not (Isa Fido Dog))
I will consider using Slot&Path-Based inference.
#{}
bobtailbot.core=> (s/assert! '(Isa Fido Dog))
wft17!: (Isa Fido Dog)
bobtailbot.core=> (s/clearkb true)
Knowledge Base cleared. Contexts, slots, caseframes, and semantic types reinitialized.
nil
bobtailbot.core=> (s/ask '(Isa Fido Dog))
I wonder if wft17?: (Isa Fido Dog)
I will consider using Slot&Path-Based inference.
I wonder if wft18?: (not (Isa Fido Dog))
I will consider using Slot&Path-Based inference.
#{}
bobtailbot.core=> quit
Bye for now!
$
```




# LICENSE


[University of Buffalo Public License](https://cse.buffalo.edu/sneps/ubpl.pdf).
