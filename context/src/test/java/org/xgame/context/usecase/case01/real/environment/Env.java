package org.xgame.context.usecase.case01.real.environment;

import org.xgame.context.usecase.case01.domain.Domain;
import org.xgame.context.usecase.case01.real.Real;

/**
 * @author dmitry.mamonov
 *         Created: 2014-08-18 9:03 PM
 */
public abstract class Env extends Real {
    protected Env() {
        field(Domain.XY.class);
        declare(Domain.Texture.class).texture(getClass().getSimpleName());
    }

    public static abstract class Surface extends Env {
        protected Surface() {
            declare(Domain.Z.class).z(0);
        }

        public static abstract class Solid extends Surface {
            protected Solid() {
                declare(Domain.CanStandOn.class);
                declare(Domain.Slip.class).ratio(1.0);
            }

            public static final class Sand extends Solid {
                public Sand() {
                    field(Domain.Slip.class).ratio(0.3);
                }
            }

            public static final class Scree extends Solid {
                public Scree() {
                    field(Domain.Slip.class).ratio(0.6);
                }
            }

            public static final class Soil extends Solid {
                public Soil() {
                    field(Domain.Slip.class).ratio(0.8);
                }
            }
        }

        public static abstract class Liquid extends Surface {
            protected Liquid() {
                //температура замерзания
                //
            }

            public static final class Water extends Liquid {
                public Water() {
                }
            }
        }
    }

    public static abstract class Barrier extends Env {
        protected Barrier() {
        }

        public static final class Rock extends Barrier {
            public Rock() {
            }
        }
    }

    public static abstract class Cover extends Env {
        protected Cover() {
        }

        public static final class Fog extends Cover {
            public Fog() {
            }
        }
    }
}
