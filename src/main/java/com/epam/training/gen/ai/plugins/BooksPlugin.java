package com.epam.training.gen.ai.plugins;

import com.epam.training.gen.ai.dto.BookDto;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class BooksPlugin {
    // Mock data for the books
    private final Map<Integer, BookDto> books = new HashMap<>();

    public BooksPlugin() {
        books.put(1, new BookDto("Open Veins of Latin America: Five Centuries of the Pillage of a Continent", "Eduardo Galeano", 1971));
        books.put(2, new BookDto("The Conquest of New Spain", "Bernal Díaz del Castillo", 1632));
        books.put(3, new BookDto("The Memory of Fire Trilogy", "Eduardo Galeano", 1982));
        books.put(4, new BookDto("The Motorcycle Diaries", "Ernesto Che Guevara", 1993));
        books.put(5, new BookDto("Bury My Heart at Wounded Knee: An Indian History of the American West", "Dee Brown", 1970));
        books.put(6, new BookDto("In the Time of the Butterflies", "Julia Alvarez", 1994));
        books.put(7, new BookDto("1491: New Revelations of the Americas Before Columbus", "Charles C. Mann", 2005));
        books.put(8, new BookDto("The Penguin History of Latin America", "Edwin Williamson", 2010));
        books.put(9, new BookDto("The Making of Modern Colombia: A Nation in Spite of Itself", "David Bushnell", 1993));
        books.put(10, new BookDto("The War of the End of the World", "Mario Vargas Llosa", 1981));
        books.put(11, new BookDto("The Bolivian Diary", "Ernesto Che Guevara", 2006));
        books.put(12, new BookDto("The Feast of the Goat", "Mario Vargas Llosa", 2000));
        books.put(13, new BookDto("The Labyrinth of Solitude", "Octavio Paz", 1950));
        books.put(14, new BookDto("One Hundred Years of Solitude", "Gabriel García Márquez", 1967));
        books.put(15, new BookDto("The Age of Capital: 1848-1875", "Eric Hobsbawm", 1975));
        books.put(16, new BookDto("The Lost City of the Monkey God: A True Story", "Douglas Preston", 2017));
        books.put(17, new BookDto("Mountains Beyond Mountains: The Quest of Dr. Paul Farmer, A Man Who Would Cure the World", "Tracy Kidder", 2003));
        books.put(18, new BookDto("The Penguin History of Modern Latin America", "Edwin Williamson", 2009));
        books.put(19, new BookDto("The Guardian of the Dead", "Sabrina Vourvoulias", 2012));
        books.put(20, new BookDto("The Maya: A Very Short Introduction", "Matthew Restall", 2018));
        books.put(21, new BookDto("The Invention of Nature: Alexander von Humboldt's New World", "Andrea Wulf", 2015));
        books.put(22, new BookDto("The Story of Mexico", "Janet Long-Solis", 2017));
        books.put(23, new BookDto("Empire's Workshop: Latin America, the United States, and the Rise of the New Imperialism", "Greg Grandin", 2006));
        books.put(24, new BookDto("The Penguin History of the United States of America", "Hugh Brogan", 1990));
        books.put(25, new BookDto("The Old Gringo", "Carlos Fuentes", 1985));
    }

    /**
     * Gets a list of books about latin american history, Usefull for avoiding model hallucinations
     * @return list of books
     */
    @DefineKernelFunction(name = "get_books", description = "Gets a list of books about latin american history")
    public List<BookDto> getBooks() {
        log.info("Getting latin american history Books");
        return new ArrayList<>(books.values());
    }
}
