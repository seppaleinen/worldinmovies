from app.models import Movie
import app.proto.movies_pb2
from app.proto.movies_pb2_grpc import MoviesControllerServicer


class MoviesControllerServicer(MoviesControllerServicer):
    """Missing associated documentation comment in .proto file."""

    def GetMovies(self, request_iterator, context):
        """Missing associated documentation comment in .proto file."""
        for request in request_iterator:
            print(f"Req: {request}")
            yield app.proto.movies_pb2.Movie(**Movie.objects.get(pk=request).data)
