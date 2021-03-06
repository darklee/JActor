package org.agilewiki.jactor.iterator.factorial;

import org.agilewiki.jactor.JAIterator;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.ResponseProcessor;
import org.agilewiki.jactor.lpc.JLPCActor;

public class Factorial extends JLPCActor {
    public Factorial(Mailbox mailbox) {
        super(mailbox);
    }

    @Override
    public void processRequest(Object req, final ResponseProcessor rp)
            throws Exception {
        final int max = 5;
        ResponseProcessor printResult = new ResponseProcessor() {
            public void process(Object rsp) throws Exception {
                System.out.println(rsp);
                rp.process(null);
            }
        };
        (new JAIterator() {
            int i;
            int r = 1;
            Multiplier mp = new Multiplier(getMailbox());

            public void process(ResponseProcessor rp) throws Exception {
                if (i >= max) rp.process(new Integer(r));
                else {
                    i += 1;
                    Multiply m = new Multiply();
                    m.a = r;
                    m.b = i;
                    send(mp, m, new ResponseProcessor() {
                        public void process(Object rsp) throws Exception {
                            r = ((Integer) rsp).intValue();
                        }
                    });
                    rp.process(null);
                }
            }
        }).iterate(printResult);
    }
}
