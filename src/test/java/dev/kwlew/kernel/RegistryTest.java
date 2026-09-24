package dev.kwlew.kernel;

import dev.kwlew.exceptions.CircularDependencyException;
import dev.kwlew.exceptions.UnresolvedDependencyException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegistryTest {

    static class Leaf {}

    static class Middle {
        final Leaf leaf;

        Middle(Leaf leaf) {
            this.leaf = leaf;
        }
    }

    static class Top {
        final Middle middle;
        final Leaf leaf;

        Top(Middle middle, Leaf leaf) {
            this.middle = middle;
            this.leaf = leaf;
        }
    }

    interface Service {}

    static class ServiceImpl implements Service {}

    static class NeedsService {
        NeedsService(Service service) {}
    }

    static class CycleA {
        CycleA(CycleB b) {}
    }

    static class CycleB {
        CycleB(CycleA a) {}
    }

    static class Exploding {
        Exploding() {
            throw new IllegalStateException("boom");
        }
    }

    static class Annotated {
        final boolean injected;

        Annotated() {
            this.injected = false;
        }

        @Inject
        Annotated(Leaf leaf) {
            this.injected = true;
        }
    }

    @Test
    void resolvesDependencyGraphAsSingletons() {
        Registry registry = new Registry();

        Top top = registry.resolve(Top.class);

        assertSame(top.leaf, top.middle.leaf);
        assertSame(top, registry.resolve(Top.class));
        assertSame(top.middle, registry.resolve(Middle.class));
    }

    @Test
    void ordersInstancesByCreation() {
        Registry registry = new Registry();

        Top top = registry.resolve(Top.class);
        Middle middle = registry.resolve(Middle.class);
        Leaf leaf = registry.resolve(Leaf.class);

        assertEquals(List.of(leaf, middle, top), registry.getAll());
        assertEquals(List.of(top, middle, leaf), registry.getAllReversed());
    }

    @Test
    void registeredInstancesAreReused() {
        Registry registry = new Registry();
        Leaf leaf = new Leaf();
        registry.register(Leaf.class, leaf);

        assertSame(leaf, registry.resolve(Middle.class).leaf);
    }

    @Test
    void bindMapsAbstractionToSingleTrackedInstance() {
        Registry registry = new Registry();
        registry.bind(Service.class, ServiceImpl.class);

        assertSame(registry.resolve(ServiceImpl.class), registry.resolve(Service.class));
        assertEquals(1, registry.getAll().size());
        assertDoesNotThrow(() -> registry.resolve(NeedsService.class));
    }

    @Test
    void unboundInterfaceDependencyFails() {
        Registry registry = new Registry();

        assertThrows(UnresolvedDependencyException.class, () -> registry.resolve(NeedsService.class));
    }

    @Test
    void detectsCircularDependencies() {
        Registry registry = new Registry();

        CircularDependencyException e = assertThrows(CircularDependencyException.class,
                () -> registry.resolve(CycleA.class));
        assertTrue(e.getMessage().contains("CycleA -> CycleB -> CycleA"), e.getMessage());
    }

    @Test
    void surfacesConstructorFailureCause() {
        Registry registry = new Registry();

        RuntimeException e = assertThrows(RuntimeException.class, () -> registry.resolve(Exploding.class));
        assertInstanceOf(IllegalStateException.class, e.getCause());
        assertEquals("boom", e.getCause().getMessage());
    }

    @Test
    void prefersInjectAnnotatedConstructor() {
        Registry registry = new Registry();

        assertTrue(registry.resolve(Annotated.class).injected);
    }

    @Test
    void sealedRegistryOnlyServesExistingInstances() {
        Registry registry = new Registry();
        Leaf leaf = registry.resolve(Leaf.class);
        registry.seal();

        assertSame(leaf, registry.resolve(Leaf.class));
        assertThrows(IllegalStateException.class, () -> registry.resolve(Middle.class));
    }
}
