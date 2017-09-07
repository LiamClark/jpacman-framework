package nl.tudelft.jpacman.board;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SquareTest {

    @Test
    void direction_for_neighbour_test() {
        Square square = new Ground(null, 0,0);
        Square east = new Ground(null, 1, 0);
        
        square.link(east, Direction.EAST);
        assertThat(square.directionForNeighbour(east)).contains(Direction.EAST);
    }

    
}
