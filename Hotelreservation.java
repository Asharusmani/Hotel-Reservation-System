import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

class Room {
    private int id;
    private String type;
    private double pricePerNight;
    private boolean isAvailable;

    public Room(int id, String type, double pricePerNight) {
        this.id = id;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.isAvailable = true;
    }

    public int getId() { return id; }
    public String getType() { return type; }
    public double getPricePerNight() { return pricePerNight; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", pricePerNight=" + pricePerNight +
                ", isAvailable=" + isAvailable +
                '}';
    }
}

class Reservation {
    private int id;
    private int roomId;
    private String userName;
    private Date checkInDate;
    private Date checkOutDate;
    private double totalPrice;

    public Reservation(int id, int roomId, String userName, Date checkInDate, Date checkOutDate, double totalPrice) {
        this.id = id;
        this.roomId = roomId;
        this.userName = userName;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalPrice = totalPrice;
    }

    public int getId() { return id; }
    public int getRoomId() { return roomId; }
    public String getUserName() { return userName; }
    public Date getCheckInDate() { return checkInDate; }
    public Date getCheckOutDate() { return checkOutDate; }
    public double getTotalPrice() { return totalPrice; }

    
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", roomId=" + roomId +
                ", userName='" + userName + '\'' +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", totalPrice=" + totalPrice +
                '}';
    }
}

class RoomService {
    private List<Room> rooms = new ArrayList<>();

    public RoomService() {
        rooms.add(new Room(1, "Single", 100.0));
        rooms.add(new Room(2, "Double", 150.0));
        rooms.add(new Room(3, "Suite", 300.0));
    }

    public List<Room> searchRooms(String type) {
        return rooms.stream()
                .filter(room -> room.isAvailable() && room.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    public Room getRoomById(int id) {
        return rooms.stream().filter(room -> room.getId() == id).findFirst().orElse(null);
    }

    public void updateRoomAvailability(int id, boolean isAvailable) {
        Room room = getRoomById(id);
        if (room != null) {
            room.setAvailable(isAvailable);
        }
    }
}

class ReservationService {
    private List<Reservation> reservations = new ArrayList<>();
    private RoomService roomService;

    public ReservationService(RoomService roomService) {
        this.roomService = roomService;
    }

    public Reservation makeReservation(int roomId, String userName, Date checkInDate, Date checkOutDate) {
        Room room = roomService.getRoomById(roomId);
        if (room != null && room.isAvailable()) {
            long diffInMillies = checkOutDate.getTime() - checkInDate.getTime();
            long diff = Math.max(1, diffInMillies / (1000 * 60 * 60 * 24));
            double totalPrice = diff * room.getPricePerNight();
            Reservation reservation = new Reservation(reservations.size() + 1, roomId, userName, checkInDate, checkOutDate, totalPrice);
            reservations.add(reservation);
            roomService.updateRoomAvailability(roomId, false);
            return reservation;
        }
        return null;
    }

    public List<Reservation> getReservationsByUserName(String userName) {
        return reservations.stream().filter(reservation -> reservation.getUserName().equalsIgnoreCase(userName)).collect(Collectors.toList());
    }
}

class PaymentService {
    public boolean processPayment(String paymentDetails, double amount) {
        System.out.println("Processing payment of $" + amount + " with details: " + paymentDetails);
        return true;
    }
}

public class HotelReservation {
    public static void main(String[] args) throws ParseException {
        RoomService roomService = new RoomService();
        ReservationService reservationService = new ReservationService(roomService);
        PaymentService paymentService = new PaymentService();

        Scanner scanner = new Scanner(System.in);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        while (true) {
            System.out.println("Welcome to the Hotel ");
            System.out.println("1. Search for rooms");
            System.out.println("2. Make a reservation");
            System.out.println("3. View your reservations");
            System.out.println("4. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine();  

            switch (choice) {
                case 1:
                    System.out.print("Enter room type (Single, Double, Suite): ");
                    String roomType = scanner.nextLine();
                    List<Room> availableRooms = roomService.searchRooms(roomType);
                    if (availableRooms.isEmpty()) {
                        System.out.println("No available rooms found for the given type.");
                    } else {
                        System.out.println("Available rooms:");
                        for (Room room : availableRooms) {
                            System.out.println(room);
                        }
                    }
                    break;
                case 2:
                    System.out.print("Enter room ID: ");
                    int roomId = scanner.nextInt();
                    scanner.nextLine();  // Consume newline
                    System.out.print("Enter your name: ");
                    String userName = scanner.nextLine();
                    System.out.print("Enter check-in date (yyyy-MM-dd): ");
                    Date checkInDate = dateFormat.parse(scanner.nextLine());
                    System.out.print("Enter check-out date (yyyy-MM-dd): ");
                    Date checkOutDate = dateFormat.parse(scanner.nextLine());
                    Reservation reservation = reservationService.makeReservation(roomId, userName, checkInDate, checkOutDate);
                    if (reservation != null) {
                        System.out.print("Enter payment details: ");
                        String paymentDetails = scanner.nextLine();
                        if (paymentService.processPayment(paymentDetails, reservation.getTotalPrice())) {
                            System.out.println("Reservation successful! Total Price: $" + reservation.getTotalPrice());
                        } else {
                            System.out.println("Payment failed. Reservation not completed.");
                        }
                    } else {
                        System.out.println("Failed to make reservation. Room may not be available.");
                    }
                    break;
                case 3:
                    System.out.print("Enter your name: ");
                    String name = scanner.nextLine();
                    List<Reservation> userReservations = reservationService.getReservationsByUserName(name);
                    if (userReservations.isEmpty()) {
                        System.out.println("No reservations found.");
                    } else {
                        System.out.println("Your reservations:");
                        for (Reservation res : userReservations) {
                            System.out.println(res);
                        }
                    }
                    break;
                case 4:
                    System.out.println("Thank you for using the Hotel!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
