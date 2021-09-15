import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class RegularExpressionsMenu {
    public static void main( String args[] ) {
        int selection = 1;
        String text = new String( "...They say two-thousand-zero-zero party over\n\tOops out of time\n\tSo tonight I'm gonna party like it's 1999..." );
        String re = new String( "\\d" );
        String some_tokens[];

        Pattern pattern = Pattern.compile(re);
        Scanner sc = new Scanner(System.in);

        System.out.println();
        while ( selection > 0 ) {
            System.out.println( "** MENU **" );
            System.out.println( " (0) Exit" );
            System.out.println( " (1) Enter Text" );
            System.out.println( " (2) Enter RE" );
            System.out.println( " (3) Match RE" );
            System.out.println( " (4) RE Tokens" );
            System.out.print( "Enter Selection:  " );
            selection = Integer.parseInt( sc.nextLine() );
            System.out.println();

            if ( selection == 0 ) {
                // Exit, no action
            }
            else if ( selection == 1 ) {
                // Enter Text
                System.out.println( "Current Text:  " );
                System.out.println( text );
                System.out.println();

                System.out.print( "Enter Text:  " );
                text = sc.nextLine();
                System.out.println();
            }
            else if ( selection == 2 ) {
                // Enter RE
                System.out.println( "Current RE:  " + re );
                System.out.println();

                System.out.print( "Enter RE:  " );
                re = sc.nextLine();
                System.out.println();
                pattern = Pattern.compile(re);
            }
            else if ( selection == 3 ) {
                // Match RE
                System.out.println( "Current Text:  " );
                System.out.println( text );
                System.out.println();

                System.out.println( "Current RE:  " + re );
                System.out.println();

                Matcher match = pattern.matcher(text);
                int count = 0;
                while ( match.find() ) {
                    System.out.println( "* Match #" + ++count + " *" );
                    System.out.println( "Matching String:   " + match.group() );
                    System.out.println( "Starting Index:    " + match.start() );
                    System.out.println( "Ending Index:      " + match.end() );
                }
                System.out.println();
            }
            else if ( selection == 4 ) {
                // RE Tokens
                System.out.println( "Current Text:  " );
                System.out.println( text );
                System.out.println();

                System.out.println( "Current RE:  " + re );
                System.out.println();

                some_tokens = pattern.split(text);
                print_some_tokens(some_tokens);
                System.out.println();
            }
        }
    }
    public static void print_some_tokens( String some_tokens[] ) {
        for ( int i = 0; i < some_tokens.length; i++ )  {
            System.out.println( "tokens[" + i + "]:  " + some_tokens[i] );
        }
    }
}
