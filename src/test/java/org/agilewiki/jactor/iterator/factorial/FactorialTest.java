package org.agilewiki.jactor.iterator.factorial;

import junit.framework.TestCase;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.MailboxFactory;

public class FactorialTest extends TestCase {
    public void testFactorial() throws Exception {
        MailboxFactory mailboxFactory = JAMailboxFactory.newMailboxFactory(1);
        try {
            Factorial factorial = new Factorial(mailboxFactory.createMailbox());
            JAFuture future = new JAFuture();
            future.send(factorial, null);
        } finally {
            mailboxFactory.close();
        }
    }
}
